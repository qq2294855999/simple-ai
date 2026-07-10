import axios, { type AxiosRequestConfig } from "axios";
import type { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import type { R } from "../dto/common/R";
import { ToastUtil } from "../utils/ToastUtil";

const instance = axios.create({
  baseURL: "/api",
  timeout: 15000
});

instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
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
