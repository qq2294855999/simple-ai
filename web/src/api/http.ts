import axios, { type AxiosRequestConfig } from "axios";
import type { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import type { R } from "../dto/common/R";
import { ToastUtil } from "../utils/ToastUtil";

const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";

/** 前端 REST 与 SSE 统一 API 根路径。 */
export const API_BASE_URL = "/api";

/** 标记请求是否已尝试过刷新 token，防止无限循环 */
interface RetryConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

/** 全局刷新锁，防止多个请求同时触发 token 刷新 */
let isRefreshing = false;

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
 * 构建 OAuth 登录页跳转 URL，携带客户端自定义文案参数。
 */
export function buildOauthLoginUrl(): string {
  const baseUrl = import.meta.env.VITE_OAUTH_LOGIN_URL || "http://localhost:8000/login";
  const params = new URLSearchParams();
  const title = import.meta.env.VITE_LOGIN_TITLE;
  const subtitle = import.meta.env.VITE_LOGIN_SUBTITLE;
  const footerTip = import.meta.env.VITE_LOGIN_FOOTER_TIP;
  if (title) params.set("title", title);
  if (subtitle) params.set("subtitle", subtitle);
  if (footerTip) params.set("footerTip", footerTip);
  // 登录成功后回跳地址
  params.set("redirect_uri", window.location.origin);
  const query = params.toString();
  return query ? `${baseUrl}?${query}` : baseUrl;
}

/**
 * 清除本地存储的认证信息并跳转 OAuth 登录页。
 * 使用 location.replace 避免浏览器历史记录中残留当前页。
 *
 * @author qty
 */
export function clearAndRedirectToLogin(): never {
  window.localStorage.removeItem(ACCESS_TOKEN_KEY);
  window.localStorage.removeItem(REFRESH_TOKEN_KEY);
  window.location.replace(buildOauthLoginUrl());
  // 阻止后续代码执行，确保跳转不被异步操作中断
  throw new Error("redirecting to login");
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
 */
async function refreshAccessToken(): Promise<{ accessToken: string; refreshToken: string }> {
  const refreshToken = window.localStorage.getItem(REFRESH_TOKEN_KEY);
  if (!refreshToken) {
    throw new Error("无刷新令牌");
  }

  const response = await oauthInstance.post<R<{ accessToken: string; refreshToken: string }>>(
    "/auth/refresh",
    { refresh: refreshToken }
  );

  const data = response.data.data;
  if (!data || !data.accessToken) {
    throw new Error("刷新 token 返回无效数据");
  }
  return data;
}

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

      // 释放刷新锁（必须在重试原始请求之前，避免竞态窗口）
      isRefreshing = false;

      // 用新 token 重试原始请求
      config.headers.set("Authorization", `Bearer ${result.accessToken}`);
      return instance(config) as Promise<never>;
    })
    .catch((refreshError) => {
      // 刷新失败，拒绝所有等待中的请求
      const error = refreshError instanceof Error ? refreshError : new Error("token 刷新失败");
      rejectSubscribers(error);

      // 释放刷新锁
      isRefreshing = false;

      // 清除存储并跳转登录页
      clearAndRedirectToLogin();
      return Promise.reject(error);
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
