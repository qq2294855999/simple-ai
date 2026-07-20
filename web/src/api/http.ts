import axios, { type AxiosRequestConfig } from "axios";
import type { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import type { R } from "../dto/common/R";
import { ToastUtil } from "../utils/ToastUtil";

const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";
const NICKNAME_KEY = "nickname";
const AVATAR_URL_KEY = "avatarUrl";

/** 前端 REST 与 SSE 统一 API 根路径。 */
export const API_BASE_URL = "/api";

/** 标记请求是否已尝试过刷新 token，防止无限循环 */
interface RetryConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

/** 全局刷新锁，防止多个请求同时触发 token 刷新 */
let isRefreshing = false;

/** 正在执行退出登录流程的标记，防止残留请求再次触发跳转 */
let isLoggingOut = false;

/**
 * 设置退出登录标记，供外部模块在清除 token 前调用，
 * 防止残留业务请求触发 handleAuthError → clearAndRedirectToLogin 连锁跳转。
 *
 * @param value true 表示正在退出登录
 * @author qty
 */
export function setIsLoggingOut(value: boolean): void {
  isLoggingOut = value;
}

/** 等待刷新完成的请求队列，存储 resolve/reject 对以支持成功和失败两种通知 */
let refreshSubscribers: Array<{
  resolve: (token: string) => void;
  reject: (error: Error) => void;
}> = [];

/**
 * 通知所有等待中的请求 token 刷新成功，让它们用新 token 重试。
 *
 * @param token 刷新后的新 access token
 */
function resolveSubscribers(token: string) {
  refreshSubscribers.forEach((s) => s.resolve(token));
  refreshSubscribers = [];
}

/**
 * 通知所有等待中的请求 token 刷新失败，让它们以错误状态结束。
 *
 * @param error 刷新失败的错误信息
 */
function rejectSubscribers(error: Error) {
  refreshSubscribers.forEach((s) => s.reject(error));
  refreshSubscribers = [];
}

/**
 * 从 localStorage 获取当前 access token，供 SSE 等非 axios 请求使用。
 *
 * @return access token 字符串，不存在时返回 null
 * @author qty
 */
export function getAccessToken(): string | null {
  return window.localStorage.getItem(ACCESS_TOKEN_KEY);
}

/**
 * 构建 OAuth 登录页跳转 URL，携带客户端自定义文案参数。
 *
 * <p>OAuth 前端使用 HashRouter，登录页真实地址为 {@code http://host/#/login?...}，
 * 因此本方法在 baseUrl 与查询参数之间插入 {@code #/login} 前缀，
 * 确保浏览器跳转后 HashRouter 能正确匹配 {@code /login} 路由，
 * 进而由 PublicRoute 读取 redirect_uri、title 等参数完成 SSO 回调或登录页渲染。</p>
 *
 * @param extraParams 可选的额外查询参数（如 logout=true 通知 OAuth 清除 session）
 * @return 完整的 OAuth 登录页跳转 URL
 * @author qty
 */
export function buildOauthLoginUrl(extraParams?: Record<string, string>): string {
  // 读取 OAuth 前端基础地址，默认回退到本地 8000 端口（不含 /login，由 #/login 拼接）
  const baseUrl = import.meta.env.VITE_OAUTH_LOGIN_URL || "http://localhost:8000";
  const params = new URLSearchParams();

  // 携带客户端自定义文案，供 OAuth 登录页展示
  const title = import.meta.env.VITE_LOGIN_TITLE;
  const subtitle = import.meta.env.VITE_LOGIN_SUBTITLE;
  const footerTip = import.meta.env.VITE_LOGIN_FOOTER_TIP;
  if (title) params.set("title", title);
  if (subtitle) params.set("subtitle", subtitle);
  if (footerTip) params.set("footerTip", footerTip);

  // 登录成功后回跳地址，OAuth 登录成功后携带 token 回调到此地址
  params.set("redirect_uri", window.location.origin);

  // 附加额外参数（如退出登录标记 logout=true、提示信息 message）
  if (extraParams) {
    Object.entries(extraParams).forEach(([key, value]) => {
      params.set(key, value);
    });
  }

  const query = params.toString();
  // OAuth 前端为 HashRouter，需拼接 #/login 前缀使路由可匹配
  return query ? `${baseUrl}#/login?${query}` : `${baseUrl}#/login`;
}

/**
 * 清除本地存储的全部认证信息并跳转 OAuth 登录页。
 * 使用 location.replace 避免浏览器历史记录中残留当前页。
 * 不再使用 throw 阻断执行，调用方需自行 return Promise.reject 终止 Promise 链。
 *
 * @param message 可选的提示信息，如"登录已过期，请重新登录"
 * @author qty
 */
export function clearAndRedirectToLogin(message?: string): void {
  // 设置退出标记，防止残留请求再次触发跳转
  isLoggingOut = true;
  window.localStorage.removeItem(ACCESS_TOKEN_KEY);
  window.localStorage.removeItem(REFRESH_TOKEN_KEY);
  // 清除用户信息，避免残留过期数据
  window.localStorage.removeItem(NICKNAME_KEY);
  window.localStorage.removeItem(AVATAR_URL_KEY);
  // 构建跳转 URL，携带提示信息
  const extraParams = message ? { message } : undefined;
  window.location.replace(buildOauthLoginUrl(extraParams));
}

/**
 * 获取 OAuth 认证服务器基础 URL。
 * 开发环境读取 VITE_OAUTH_SERVER_URL，生产环境使用当前页面同源地址。
 *
 * @author qty
 */
export function getOauthServerUrl(): string {
  if (import.meta.env.DEV) {
    return import.meta.env.VITE_OAUTH_SERVER_URL || "http://localhost:8000";
  }
  return window.location.origin;
}

/**
 * 从环境变量读取客户端凭证并编码为 Basic Auth 头。
 * VITE_CLIENT_ID 和 VITE_CLIENT_SECRET 必须与 OAuth 服务端 sys_project_client 表一致。
 */
function buildBasicAuth(): string {
  const clientId = import.meta.env.VITE_CLIENT_ID || "oauth";
  const clientSecret = import.meta.env.VITE_CLIENT_SECRET || "123456";
  return btoa(`${clientId}:${clientSecret}`);
}

// ====================== OAuth 认证 axios 实例 ======================

/** OAuth 认证服务器的 axios 实例，用于登录/刷新/退出等认证接口 */
const oauthInstance = axios.create({
  baseURL: getOauthServerUrl(),
  timeout: 15000,
});

/**
 * OAuth 请求拦截器：对认证接口自动附加 Basic Auth。
 * 仿照 simple-common-oauth 前端的 authApi.ts 做法。
 */
oauthInstance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const url = config.url || "";

  // 精确匹配：登录和刷新接口使用 Basic Auth（clientId:clientSecret）
  if (url === "/auth/login" || url === "/auth/refresh") {
    const basicAuth = buildBasicAuth();
    config.headers.set("Authorization", `Basic ${basicAuth}`);
  }

  return config;
});

/**
 * 调用 OAuth 刷新 token 接口。
 * 使用 oauthInstance，Basic Auth 由请求拦截器自动附加。
 *
 * @return 新的 accessToken 和 refreshToken
 * @author qty
 */
async function refreshAccessToken(): Promise<{ accessToken: string; refreshToken: string }> {
  const currentRefreshToken = window.localStorage.getItem(REFRESH_TOKEN_KEY);
  if (!currentRefreshToken) {
    throw new Error("无刷新令牌");
  }

  const response = await oauthInstance.post<R<{ accessToken: string; refreshToken: string }>>(
    "/auth/refresh",
    { refresh: currentRefreshToken }
  );

  const data = response.data.data;
  if (!data || !data.accessToken) {
    throw new Error("刷新 token 返回无效数据");
  }
  return data;
}

// OAuth 实例响应拦截器：处理刷新接口返回的业务错误
oauthInstance.interceptors.response.use(
  (response) => response,
  (error: AxiosError<R<unknown>>) => {
    // 刷新接口本身失败，说明 refresh token 也无效，直接跳转登录
    if (error.response?.status === 401 || error.response?.status === 400) {
      clearAndRedirectToLogin();
    }
    return Promise.reject(error);
  }
);

// ====================== 业务 API axios 实例 ======================

/** 业务 API 的 axios 实例，baseURL 为 /api */
const instance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
});

/**
 * 获取业务请求统一使用的 Authorization 请求头。
 *
 * @author qty
 */
export function getBusinessAuthorizationHeader(): Record<string, string> {
  const accessToken = window.localStorage.getItem(ACCESS_TOKEN_KEY);
  if (!accessToken) {
    return {};
  }
  return { Authorization: `Bearer ${accessToken}` };
}

// 业务请求拦截器：自动附加 Bearer token
instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const authorizationHeader = getBusinessAuthorizationHeader();
  if (authorizationHeader.Authorization) {
    config.headers.set("Authorization", authorizationHeader.Authorization);
  }
  return config;
});

// 业务响应拦截器：token 过期先刷新，刷新失败再跳转登录
instance.interceptors.response.use(
  <T>(response: AxiosResponse<R<T>>) => {
    const body = response.data;

    // 正常返回
    if (body.code === "200") {
      return body.data as T;
    }

    // token 过期或未登录，尝试刷新
    if (body.code === "1000" || body.code === "1001") {
      return handleAuthError(response.config as RetryConfig);
    }

    // 其他业务错误
    ToastUtil.error(body.message || "请求失败");
    return Promise.reject(new Error(body.message || "请求失败"));
  },
  (error: AxiosError<R<unknown>>) => {
    // 后端全局异常处理器将 DefaultException 映射为 HTTP 500，
    // 响应体中仍携带业务错误码（1000/1001），需在此检查
    const businessCode = error.response?.data?.code;
    if (businessCode === "1000" || businessCode === "1001") {
      return handleAuthError(error.config as RetryConfig);
    }

    // HTTP 401，尝试刷新 token
    if (error.response?.status === 401) {
      return handleAuthError(error.config as RetryConfig);
    }

    const responseMessage = error.response?.data?.message;
    const errorMessage = responseMessage || error.message || "网络异常";
    ToastUtil.error(errorMessage);
    return Promise.reject(error);
  }
);

// ====================== 认证错误处理 ======================

/**
 * 处理认证错误：先尝试刷新 token，失败后跳转登录页。
 *
 * @param config 原始请求配置
 */
function handleAuthError(config: RetryConfig): Promise<never> {
  // 正在退出登录，静默拒绝残留请求，避免二次跳转
  if (isLoggingOut) {
    return Promise.reject(new Error("正在退出登录"));
  }

  const refreshToken = window.localStorage.getItem(REFRESH_TOKEN_KEY);

  // 无刷新令牌，直接跳转登录
  if (!refreshToken) {
    clearAndRedirectToLogin();
    return Promise.reject(new Error("未登录"));
  }

  // 已重试过，不再重复刷新
  if (config._retry) {
    clearAndRedirectToLogin();
    return Promise.reject(new Error("token 刷新失败"));
  }

  config._retry = true;

  // 正在刷新中，将请求加入等待队列
  if (isRefreshing) {
    return new Promise((_resolve, _reject) => {
      refreshSubscribers.push({
        resolve: (newToken: string) => {
          // 刷新成功后用新 token 重试
          config.headers.set("Authorization", `Bearer ${newToken}`);
          instance(config)
            .then((res) => _resolve(res as never))
            .catch((err) => _reject(err));
        },
        reject: (error: Error) => {
          // 刷新失败，拒绝等待中的请求
          _reject(error);
        },
      });
    }) as Promise<never>;
  }

  // 获取刷新锁
  isRefreshing = true;

  return refreshAccessToken()
    .then((result) => {
      // 刷新成功，更新存储
      window.localStorage.setItem(ACCESS_TOKEN_KEY, result.accessToken);
      window.localStorage.setItem(REFRESH_TOKEN_KEY, result.refreshToken);

      // 通知等待队列中的所有请求刷新成功
      resolveSubscribers(result.accessToken);

      // 用新 token 重试原始请求
      config.headers.set("Authorization", `Bearer ${result.accessToken}`);
      return instance(config) as Promise<never>;
    })
    .catch((refreshError) => {
      // 刷新失败，拒绝所有等待中的请求
      const error = refreshError instanceof Error ? refreshError : new Error("token 刷新失败");
      rejectSubscribers(error);

      // 清除存储并跳转登录页
      clearAndRedirectToLogin();
      return Promise.reject(error);
    })
    .finally(() => {
      // 无论刷新成功或失败，都必须释放锁，避免死锁
      isRefreshing = false;
    });
}

// ====================== 导出 ======================

/**
 * 封装后的 http 客户端，响应拦截器已解包 R 结构，
 * 所有方法直接返回业务数据类型 T。
 *
 * @author qty
 */
export const http = {
  get: <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    instance.get(url, config) as Promise<T>,

  post: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> =>
    instance.post(url, data, config) as Promise<T>,

  put: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> =>
    instance.put(url, data, config) as Promise<T>,

  delete: <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    instance.delete(url, config) as Promise<T>,
};
