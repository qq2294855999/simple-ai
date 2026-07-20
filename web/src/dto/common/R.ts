/**
 * 后端统一响应结构。
 * 注意：本项目中 code 为 string 类型（如 "200"、"1000"），
 * 与某些规范中的 number 类型不同，请保持一致使用字符串比较。
 *
 * @author qty
 */
export interface R<T> {
  code: string;
  message: string;
  data: T;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export interface PageRequest {
  current: number;
  size: number;
  pageSort?: string;
}
