import "@testing-library/jest-dom/vitest";
import { afterEach } from "vitest";
import { cleanup } from "@testing-library/react";

/**
 * 清理组件测试产生的 DOM。
 */
afterEach(() => {
  cleanup();
});
