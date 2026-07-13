import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

/**
 * Vitest 测试配置。
 *
 * @author qty
 */
export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    setupFiles: ["./src/test/setup.ts"],
    clearMocks: true
  }
});
