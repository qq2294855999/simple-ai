import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd());
    // OAuth 认证服务地址，通过 VITE_OAUTH_SERVER_URL 环境变量配置
    const oauthTarget = env.VITE_OAUTH_SERVER_URL || "http://localhost:8000";
    return {
        plugins: [react()],
        server: {
            port: 5174,
            proxy: {
                "/api": {
                    target: "http://localhost:8001",
                    changeOrigin: true,
                    proxyTimeout: 0,
                    timeout: 0,
                    configure: proxy => {
                        // 将后端 SSE 的禁止缓冲语义透传给开发代理，避免代理聚合完整响应后再返回浏览器。
                        proxy.on("proxyReq", proxyReq => {
                            proxyReq.setHeader("Accept-Encoding", "identity");
                            proxyReq.setHeader("Cache-Control", "no-cache, no-transform");
                        });
                    },
                    rewrite: path => path.replace(/^\/api/, "")
                },
                "/auth": {
                    target: oauthTarget,
                    changeOrigin: true
                }
            }
        }
    };
});
