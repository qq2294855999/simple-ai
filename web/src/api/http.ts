import axios, { type AxiosRequestConfig } from "axios";
import type { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import type { R } from "../dto/common/R";
import { ToastUtil } from "../utils/ToastUtil";

const ACCESS_TOKEN_STORAGE_KEY = "accessToken";

/** 前端 REST 与 SSE 统一 API 根路径。 */
export const API_BASE_URL = "/api";

const instance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000
});

/**
 * 获取业务请求统一使用的 Authorization 请求头。
 *
 * 未配置登录令牌时返回空对象，保持未启用认证环境的现有请求行为。
 */
export function getBusinessAuthorizationHeader(): Record<string, string> {
  const accessToken = window.localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY);
  if (!accessToken) {
    return {};
  }
  return { Authorization: `Bearer ${accessToken}` };
}

instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const authorizationHeader = getBusinessAuthorizationHeader();
  if (authorizationHeader.Authorization) {
    config.headers.set("Authorization", authorizationHeader.Authorization);
  }
  return config;
});

instance.interceptors.response.use(
  <T>(response: AxiosResponse<R<T>>) => {
    const body = response.data;
    if (body.code !== 200) {
      ToastUtil.error(body.message || "请求失败");
      return Promise.reject(new Error(body.message || "请求失败"));
    }
    return body.data as T;
  },
  (error: AxiosError<R<unknown>>) => {
    const responseMessage = error.response?.data?.message;
    const errorMessage = responseMessage || error.message || "网络异常";
    ToastUtil.error(errorMessage);
    return Promise.reject(error);
  }
);

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
