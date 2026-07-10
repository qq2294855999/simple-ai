import React from "react";
import ReactDOM from "react-dom/client";
import { ConfigProvider } from "antd";
import zhCN from "antd/locale/zh_CN";
import { RouterProvider } from "react-router-dom";
import { AppRouter } from "./router/AppRouter";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <ConfigProvider locale={zhCN} theme={{ token: { borderRadius: 8 } }}>
      <RouterProvider router={AppRouter} />
    </ConfigProvider>
  </React.StrictMode>
);
