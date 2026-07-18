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

/** 等待刷新完成的请求队列 */
let refreshSubscribers: ((token: string) => void)[] = [];

/**
 * 通知所有等待中的请求 token 已刷新。
 */
function notifySubscribers(token: string) {
  refreshSubscribers.forEach((cb) => cb(token));
  refreshSubscribers = [];
}

/**
 * 添加等待刷新完成的回调。
 */
function addRefreshSubscriber(cb: (token: string) => void) {
  refreshSubscribers.push(cb);
}

/**
 * 构建 OAuth 登录页跳转 URL，携带客户端自定义文案参数。
 */
function buildOauthLoginUrl(): string {
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
 */
function getOauthServerUrl(): string {
  if (import.meta.env.DEV) {
    return import.meta.env.VITE_OAUTH_SERVER_URL || "http://localhost:8000";
  }
  return window.location.origin;
}

/**
 * 调用 OAuth 刷新 token 接口。
 */
async function refreshAccessToken(): Promise<{ accessToken: string; refreshToken: string }> {
  const refreshToken = window.localStorage.getItem(REFRESH_TOKEN_KEY);
  if (!refreshToken) {
    throw new Error("无刷新令牌");
  }

  // 构建 Basic Auth 请求头（clientId:clientSecret）
  const clientId = import.meta.env.VITE_CLIENT_ID || "simple-ai";
  const clientSecret = import.meta.env.VITE_CLIENT_SECRET || "123456";
  const basicAuth = btoa(`${clientId}:${clientSecret}`);

  const response = await axios.post<R<{ accessToken: string; refreshToken: string }>>(
    `${getOauthServerUrl()}/auth/refresh`,
    { refresh: refreshToken },
    { headers: { Authorization: `Basic ${basicAuth}` } }
  );

  const data = response.data.data;
  if (!data || !data.accessToken) {
    throw new Error("刷新 token 返回无效数据");
  }
  return data;
}

const instance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
});

/**
 * 获取业务请求统一使用的 Authorization 请求头。
 */
export function getBusinessAuthorizationHeader(): Record<string, string> {
  const accessToken = window.localStorage.getItem(ACCESS_TOKEN_KEY);
  if (!accessToken) {
    return {};
  }
  return { Authorization: `Bearer ${accessToken}` };
}

// 请求拦截器：自动附加 Bearer token
instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const authorizationHeader = getBusinessAuthorizationHeader();
  if (authorizationHeader.Authorization) {
    config.headers.set("Authorization", authorizationHeader.Authorization);
  }
  return config;
});

// 响应拦截器：token 过期先刷新，刷新失败再跳转登录
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

/**
 * 处理认证错误：先尝试刷新 token，失败后跳转登录页。
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

  // 正在刷新中，加入等待队列
  if (isRefreshing) {
    return new Promise((_resolve, _reject) => {
      addRefreshSubscriber((newToken: string) => {
        config.headers.set("Authorization", `Bearer ${newToken}`);
        instance(config)
          .then((res) => _resolve(res as never))
          .catch((err) => _reject(err));
      });
    }) as Promise<never>;
  }

  isRefreshing = true;

  return refreshAccessToken()
    .then((result) => {
      // 刷新成功，更新存储
      window.localStorage.setItem(ACCESS_TOKEN_KEY, result.accessToken);
      window.localStorage.setItem(REFRESH_TOKEN_KEY, result.refreshToken);

      // 通知等待队列
      notifySubscribers(result.accessToken);

      // 重试原始请求
      config.headers.set("Authorization", `Bearer ${result.accessToken}`);
      return instance(config) as Promise<never>;
    })
    .catch(() => {
      // 刷新失败，先拒绝所有等待中的请求，再清除存储跳转登录
      refreshSubscribers.forEach((cb) => cb(""));
      refreshSubscribers = [];
      clearAndRedirectToLogin();
      return Promise.reject(new Error("token 刷新失败"));
    })
    .finally(() => {
      isRefreshing = false;
    });
}

/**
 * 封装后的 http 客户端，响应拦截器已解包 R 结构，
 * 所有方法直接返回业务数据类型 T。
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
