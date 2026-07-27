import React from "react";
import ReactDOM from "react-dom/client";
import {ConfigProvider} from "antd";
import zhCN from "antd/locale/zh_CN";
import {RouterProvider} from "react-router-dom";
import {AppRouter} from "./router/AppRouter";
// 引入 MD 编辑器外壳样式，否则编辑器边框/工具栏布局异常
import "@uiw/react-md-editor/markdown-editor.css";
// 引入 MD 预览区渲染样式，否则 ol/ul 列表序号被 Tailwind Preflight 重置为 none
import "@uiw/react-markdown-preview/markdown.css";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <ConfigProvider locale={zhCN} theme={{ token: { borderRadius: 8 } }}>
      <RouterProvider router={AppRouter} />
    </ConfigProvider>
  </React.StrictMode>
);
