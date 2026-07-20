import { RobotOutlined, SendOutlined, SettingOutlined, MessageOutlined, CloudServerOutlined } from "@ant-design/icons";
import { Layout, Menu } from "antd";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Outlet, useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { getOauthServerUrl, buildOauthLoginUrl, setIsLoggingOut, clearAndRedirectToLogin } from "../../api/http";

const { Header, Sider, Content } = Layout;

const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";
const NICKNAME_KEY = "nickname";
const AVATAR_URL_KEY = "avatarUrl";

/**
 * 解码 JWT token 的 payload 并检查是否已过期。
 * 如果 token 无法解码或没有 exp 字段，视为未过期（由后续请求的错误处理接管）。
 *
 * @param token JWT access token
 * @return true 表示 token 已过期
 * @author qty
 */
function isTokenExpired(token: string): boolean {
  try {
    // JWT 格式：header.payload.signature，取中间部分解码
    const payloadBase64 = token.split(".")[1];
    if (!payloadBase64) {
      return false;
    }
    const payloadJson = atob(payloadBase64);
    const payload = JSON.parse(payloadJson);
    if (!payload.exp) {
      return false;
    }
    // exp 是秒级时间戳，转换为毫秒后与当前时间比较
    return Date.now() >= payload.exp * 1000;
  } catch {
    // 解码失败时不做预检，交给后续请求处理
    return false;
  }
}

/**
 * 根据昵称生成稳定的头像渐变色。
 */
function getAvatarColor(name: string): { bg: string; text: string } {
  const palette = [
    { bg: "linear-gradient(135deg, #1677ff, #4096ff)", text: "#fff" },
    { bg: "linear-gradient(135deg, #722ed1, #b37feb)", text: "#fff" },
    { bg: "linear-gradient(135deg, #13c2c2, #36cfc9)", text: "#fff" },
    { bg: "linear-gradient(135deg, #52c41a, #73d13d)", text: "#fff" },
    { bg: "linear-gradient(135deg, #fa8c16, #ffa940)", text: "#fff" },
    { bg: "linear-gradient(135deg, #eb2f96, #f759ab)", text: "#fff" },
    { bg: "linear-gradient(135deg, #2f54eb, #597ef7)", text: "#fff" },
    { bg: "linear-gradient(135deg, #a0d911, #bae637)", text: "#262626" }
  ];
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return palette[Math.abs(hash) % palette.length];
}

/**
 * 子菜单路由 key 到父级分组 key 的映射，用于自动展开当前页所属分组。
 */
const CHILD_TO_PARENT: Record<string, string> = {
  "/agent-design": "group-agent-config",
  "/sub-agent-relation": "group-agent-config",
  "/agent-skill": "group-agent-config",
  "/agent-rule": "group-agent-config",
  "/agent-memory": "group-agent-config",
  "/command-dispatch": "group-command",
  "/atomic-command": "group-command",
  "/task": "group-command",
  "/ai-model-provider": "group-large-model",
  "/ai-model": "group-large-model",
};

/**
 * 全局布局组件，含顶部 Header（OAuth 风格）、左侧可折叠分组菜单、右侧内容区。
 *
 * @author qty
 */
export function BasicLayoutComponent() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();

  // ====== 侧边栏状态 ======
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [openKeys, setOpenKeys] = useState<string[]>(() => {
    const parentKey = CHILD_TO_PARENT[location.pathname];
    return parentKey ? [parentKey] : [];
  });

  useEffect(() => {
    const parentKey = CHILD_TO_PARENT[location.pathname];
    if (parentKey) {
      setOpenKeys(prev => (prev.includes(parentKey) ? prev : [...prev, parentKey]));
    }
  }, [location.pathname]);

  // ====== 菜单 key 到页面名称的映射 ======
  const MENU_LABEL_MAP: Record<string, string> = {
    "/workbench": "智能体工作台",
    "/agent-chat": "人机对话",
    "/agent-design": "智能体设计管理",
    "/sub-agent-relation": "子智能体编排",
    "/agent-skill": "技能管理",
    "/agent-rule": "规则管理",
    "/agent-memory": "记忆编排管理",
    "/command-dispatch": "命令调度",
    "/atomic-command": "原子命令管理",
    "/task": "任务执行记录",
    "/ai-model-provider": "模型供应商",
    "/ai-model": "模型管理",
  };
  const currentPageName = MENU_LABEL_MAP[location.pathname] || "";

  // ====== OAuth 登录回调处理 ======
  useEffect(() => {
    const urlToken = searchParams.get("access_token");
    const urlRefresh = searchParams.get("refresh_token");
    if (urlToken) {
      window.localStorage.setItem(ACCESS_TOKEN_KEY, urlToken);
      if (urlRefresh) {
        window.localStorage.setItem(REFRESH_TOKEN_KEY, urlRefresh);
      }
      const urlNickname = searchParams.get("nickname");
      if (urlNickname) window.localStorage.setItem(NICKNAME_KEY, urlNickname);
      const urlAvatar = searchParams.get("avatar_url");
      if (urlAvatar) window.localStorage.setItem(AVATAR_URL_KEY, urlAvatar);
      const cleanUrl = window.location.origin + window.location.pathname;
      window.history.replaceState({}, "", cleanUrl);
      return;
    }

    // 检查 localStorage 中是否有有效的 access token
    const accessToken = window.localStorage.getItem(ACCESS_TOKEN_KEY);
    if (!accessToken) {
      // 无 token，跳转登录页并提示用户需要先登录
      window.location.replace(buildOauthLoginUrl({ message: "请先登录" }));
      return;
    }

    // 检查 token 是否已过期，避免第一次业务请求时才触发刷新
    if (isTokenExpired(accessToken)) {
      // token 已过期，先尝试刷新，刷新失败再跳转登录
      const refreshToken = window.localStorage.getItem(REFRESH_TOKEN_KEY);
      if (refreshToken) {
        // 有 refresh token，清除 access token 让后续请求触发刷新流程
        // 页面正常加载，子组件的数据请求会因 token 缺失而被 http 拦截器自动刷新
        window.localStorage.removeItem(ACCESS_TOKEN_KEY);
        return;
      }
      // 无 refresh token，调用统一退出函数（自动设置 isLoggingOut 标记并携带提示文案）
      clearAndRedirectToLogin("登录已过期，请重新登录");
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // ====== Header 用户信息 ======
  const [avatarDropdownOpen, setAvatarDropdownOpen] = useState(false);
  const [avatarError, setAvatarError] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const [showPasswordModal, setShowPasswordModal] = useState(false);

  const nickname = window.localStorage.getItem(NICKNAME_KEY) || "用户";
  const avatarUrl = window.localStorage.getItem(AVATAR_URL_KEY) || "";
  const avatarInitial = nickname.charAt(0).toUpperCase();
  const avatarColors = useMemo(() => getAvatarColor(nickname), [nickname]);

  useEffect(() => { setAvatarError(false); }, [avatarUrl]);

  // 点击外部关闭头像下拉
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest('#avatar-dropdown-trigger') && !target.closest('#avatar-dropdown-menu')) {
        setAvatarDropdownOpen(false);
      }
    };
    document.addEventListener("click", handler);
    return () => document.removeEventListener("click", handler);
  }, []);

  // 退出登录
  const handleLogout = useCallback(async () => {
    // 首先设置退出标记，防止残留请求在 token 清除后触发 handleAuthError 连锁跳转
    setIsLoggingOut(true);
    const accessToken = window.localStorage.getItem(ACCESS_TOKEN_KEY);
    // 先清除本地存储，防止残留请求再次触发跳转
    window.localStorage.removeItem(ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(REFRESH_TOKEN_KEY);
    window.localStorage.removeItem(NICKNAME_KEY);
    window.localStorage.removeItem(AVATAR_URL_KEY);
    // 异步通知 OAuth 服务端退出，忽略可能的网络错误
    if (accessToken) {
      try {
        await fetch(`${getOauthServerUrl()}/auth/loginOut`, {
          method: "POST",
          headers: { Authorization: `Bearer ${accessToken}` }
        });
      } catch { /* ignore */ }
    }
    // 跳转 OAuth 登录页，携带 logout=true 和退出提示
    window.location.replace(buildOauthLoginUrl({
      logout: "true",
      message: "您已退出登录"
    }));
  }, []);

  // ====== 修改密码 ======
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordLoading, setPasswordLoading] = useState(false);

  const handleChangePassword = useCallback(async () => {
    if (!oldPassword.trim()) { alert("请输入旧密码"); return; }
    if (!newPassword.trim()) { alert("请输入新密码"); return; }
    if (newPassword !== confirmPassword) { alert("两次输入的密码不一致"); return; }
    setPasswordLoading(true);
    try {
      const token = window.localStorage.getItem(ACCESS_TOKEN_KEY);
      const resp = await fetch(`${getOauthServerUrl()}/auth/api/change-password`, {
        method: "POST",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify({ oldPassword, newPassword })
      });
      if (!resp.ok) { const err = await resp.json(); throw new Error(err.message || "修改失败"); }
      alert("密码修改成功，请重新登录");
      window.localStorage.clear();
      // 密码修改成功，跳转 OAuth 登录页并提示用户重新登录
      window.location.replace(buildOauthLoginUrl({ message: "密码修改成功，请重新登录" }));
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "修改失败");
    } finally { setPasswordLoading(false); }
  }, [oldPassword, newPassword, confirmPassword]);

  // ====== 菜单项 ======
  const menuItems = useMemo(() => [
    { key: "/workbench", icon: <RobotOutlined />, label: "智能体工作台" },
    { key: "/agent-chat", icon: <MessageOutlined />, label: "人机对话" },
    {
      key: "group-agent-config", icon: <SettingOutlined />, label: "智能体配置",
      children: [
        { key: "/agent-design", label: "智能体设计管理" },
        { key: "/sub-agent-relation", label: "子智能体编排" },
        { key: "/agent-skill", label: "技能管理" },
        { key: "/agent-rule", label: "规则管理" },
        { key: "/agent-memory", label: "记忆编排管理" }
      ]
    },
    {
      key: "group-command", icon: <SendOutlined />, label: "命令与执行",
      children: [
        { key: "/command-dispatch", label: "命令调度" },
        { key: "/atomic-command", label: "原子命令管理" },
        { key: "/task", label: "任务执行记录" }
      ]
    },
    {
      key: "group-large-model", icon: <CloudServerOutlined />, label: "大模型管理",
      children: [
        { key: "/ai-model-provider", label: "模型供应商" },
        { key: "/ai-model", label: "模型管理" }
      ]
    }
  ], []);

  return (
    <Layout style={{ minHeight: "100vh" }}>
      {/* ========== Header（OAuth 风格） ========== */}
      <Header style={{
        display: "flex", alignItems: "center", justifyContent: "space-between",
        padding: "0 24px", height: 56,
        background: "linear-gradient(135deg, #001529 0%, #002140 100%)",
        boxShadow: "0 2px 8px rgba(0, 0, 0, 0.15)"
      }}>
        {/* 左侧：Logo + 侧边栏切换 */}
        <div className="flex items-center gap-3">
          {/* 侧边栏切换按钮 */}
          <button
            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
            className="p-1.5 text-white/70 hover:text-white hover:bg-white/10 rounded-lg transition"
            title={sidebarCollapsed ? "展开侧边栏" : "收起侧边栏"}
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>

          {/* Logo 区域 */}
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-md flex items-center justify-center"
              style={{ background: "linear-gradient(135deg, #1677ff, #4096ff)" }}>
              <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </div>
            <span className="font-semibold text-white text-sm hidden sm:inline">
              Simple AI 管理端
            </span>
          </div>

          {/* 分隔 + 当前页面标题 */}
          <div className="hidden md:flex items-center gap-3">
            <div className="w-px h-5 bg-white/20" />
            <span className="text-white/80 text-sm">
              {currentPageName}
            </span>
          </div>
        </div>

        {/* 右侧：用户信息区域 */}
        <div className="flex items-center gap-3">
          {/* 用户昵称 */}
          <span className="text-white/85 text-sm font-medium max-w-[120px] truncate">
            {nickname}
          </span>

          {/* 头像 + 下拉 */}
          <div ref={dropdownRef} className="relative" id="avatar-dropdown-trigger">
            <button
              onClick={() => setAvatarDropdownOpen(!avatarDropdownOpen)}
              className="flex items-center focus:outline-none relative"
              style={{ padding: 2 }}
            >
              {/* 头像 - 渐变边框，悬浮发光效果 */}
              <div style={{
                width: 38, height: 38, borderRadius: "50%",
                background: "linear-gradient(135deg, #1677ff, #4096ff, #69b1ff)",
                padding: 2,
                transform: avatarDropdownOpen ? "scale(1.05)" : "scale(1)",
                boxShadow: avatarDropdownOpen ? "0 0 12px rgba(22,119,255,0.5)" : "none",
                transition: "all 0.2s ease"
              }}
                onMouseEnter={(e) => {
                  if (!avatarDropdownOpen) {
                    e.currentTarget.style.transform = "scale(1.05)";
                    e.currentTarget.style.boxShadow = "0 0 12px rgba(22,119,255,0.4)";
                  }
                }}
                onMouseLeave={(e) => {
                  if (!avatarDropdownOpen) {
                    e.currentTarget.style.transform = "scale(1)";
                    e.currentTarget.style.boxShadow = "0 0 0 rgba(22, 119, 255, 0)";
                  }
                }}
              >
                <div style={{
                  width: "100%", height: "100%", borderRadius: "50%",
                  background: "#fff", overflow: "hidden",
                  display: "flex", alignItems: "center", justifyContent: "center"
                }}>
                  {!avatarError && avatarUrl ? (
                    <img src={avatarUrl} alt={nickname} style={{ width: "100%", height: "100%", objectFit: "cover" }}
                      onError={() => setAvatarError(true)} />
                  ) : (
                    <span style={{
                      width: "100%", height: "100%", display: "flex", alignItems: "center", justifyContent: "center",
                      background: avatarColors.bg, color: avatarColors.text, fontSize: 14, fontWeight: 700
                    }}>{avatarInitial}</span>
                  )}
                </div>
              </div>
              {/* 下拉箭头 */}
              <svg
                className={`w-3 h-3 ml-1 text-white/60 transition-transform ${avatarDropdownOpen ? 'rotate-180' : ''}`}
                fill="none" stroke="currentColor" viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </button>

          {/* 下拉菜单 */}
          {avatarDropdownOpen && (
            <div
              id="avatar-dropdown-menu"
              className="animate-slide-down absolute right-0 top-full mt-2 w-56 rounded-lg overflow-hidden z-50"
              style={{
                background: '#fff',
                boxShadow: '0 8px 24px rgba(0, 0, 0, 0.15)',
                border: '1px solid #f0f0f0'
              }}
            >
              {/* 用户信息头部 */}
              <div className="px-4 py-3 border-b border-gray-100"
                style={{ background: 'linear-gradient(135deg, #e6f4ff, #f0f5ff)' }}>
                <div className="flex items-center gap-3">
                  {/* 头像 */}
                  <div
                    className="rounded-full flex items-center justify-center flex-shrink-0"
                    style={{
                      width: '40px',
                      height: '40px',
                      background: 'linear-gradient(135deg, #1677ff, #4096ff, #69b1ff)',
                      padding: '2px'
                    }}
                  >
                    <div className="w-full h-full rounded-full bg-white flex items-center justify-center overflow-hidden">
                      {!avatarError && avatarUrl ? (
                        <img src={avatarUrl} alt={nickname} className="w-full h-full object-cover" />
                      ) : (
                        <span className="w-full h-full flex items-center justify-center text-sm font-bold"
                          style={{ background: avatarColors.bg, color: avatarColors.text }}>
                          {avatarInitial}
                        </span>
                      )}
                    </div>
                  </div>
                  {/* 昵称和角色 */}
                  <div className="min-w-0">
                    <div className="text-sm font-semibold text-gray-800 truncate">{nickname}</div>
                    <div className="text-xs text-gray-400 mt-0.5">系统用户</div>
                  </div>
                </div>
              </div>

              {/* 菜单项 */}
              <div className="py-1">
                <button
                  onClick={() => {
                    setAvatarDropdownOpen(false)
                    setShowPasswordModal(true)
                  }}
                  className="w-full text-left px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition flex items-center gap-3"
                >
                  <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                      d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                  </svg>
                  修改密码
                </button>
                <button
                  onClick={() => {
                    setAvatarDropdownOpen(false)
                    handleLogout()
                  }}
                  className="w-full text-left px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 transition flex items-center gap-3"
                >
                  <svg className="w-4 h-4 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                      d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                  退出登录
                </button>
              </div>
            </div>
          )}
          </div>
        </div>
      </Header>

      <Layout>
        {/* 侧边栏遮罩 - 小屏时收起后点击关闭 */}
        {!sidebarCollapsed && (
          <div
            className="fixed inset-0 bg-black/40 z-10 lg:hidden"
            onClick={() => setSidebarCollapsed(true)}
          />
        )}

        {/* ========== 侧边栏 ========== */}
        <Sider
          width={220}
          collapsedWidth={0}
          collapsed={sidebarCollapsed}
          theme="light"
          style={{ transition: "all 0.3s" }}
        >
          <Menu
            mode="inline" selectedKeys={[location.pathname]} openKeys={openKeys}
            onOpenChange={setOpenKeys} style={{ height: "100%", borderRight: 0 }}
            onClick={item => navigate(item.key)} items={menuItems}
          />
        </Sider>
        <Layout style={{ padding: 24 }}>
          <Content style={{ padding: 24, margin: 0, minHeight: 280, background: "#fff", borderRadius: 8 }}>
            <Outlet />
          </Content>
        </Layout>
      </Layout>

      {/* ========== 修改密码弹窗 ========== */}
      {showPasswordModal && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.5)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 100 }}>
          <div style={{ background: "#fff", borderRadius: 12, boxShadow: "0 20px 60px rgba(0,0,0,0.3)", width: "100%", maxWidth: 420, margin: "0 16px", overflow: "hidden" }}>
            <div style={{ padding: "clamp(20px, 2.5vw, 32px)" }}>
              <div style={{ textAlign: "center", marginBottom: 24 }}>
                <div style={{ width: 48, height: 48, borderRadius: "50%", margin: "0 auto 12px", display: "flex", alignItems: "center", justifyContent: "center", background: "linear-gradient(135deg, #e6f4ff, #bae0ff)" }}>
                  <svg style={{ width: 24, height: 24, color: "#1677ff" }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                  </svg>
                </div>
                <h3 style={{ fontSize: 18, fontWeight: 600, color: "#1f1f1f", margin: 0 }}>修改密码</h3>
              </div>

              <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
                <div>
                  <label style={{ display: "block", fontSize: 14, fontWeight: 500, color: "#434343", marginBottom: 6 }}>旧密码</label>
                  <input type="password" value={oldPassword} onChange={e => setOldPassword(e.target.value)}
                    placeholder="请输入旧密码"
                    style={{ width: "100%", height: 42, padding: "0 14px", border: "1px solid #d9d9d9", borderRadius: 8, fontSize: 14, outline: "none", background: "#fafafa", boxSizing: "border-box" }}
                    onFocus={e => { e.target.style.borderColor = "#1677ff"; e.target.style.background = "#fff"; }}
                    onBlur={e => { e.target.style.borderColor = "#d9d9d9"; e.target.style.background = "#fafafa"; }}
                  />
                </div>
                <div>
                  <label style={{ display: "block", fontSize: 14, fontWeight: 500, color: "#434343", marginBottom: 6 }}>新密码</label>
                  <input type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)}
                    placeholder="请输入新密码"
                    style={{ width: "100%", height: 42, padding: "0 14px", border: "1px solid #d9d9d9", borderRadius: 8, fontSize: 14, outline: "none", background: "#fafafa", boxSizing: "border-box" }}
                    onFocus={e => { e.target.style.borderColor = "#1677ff"; e.target.style.background = "#fff"; }}
                    onBlur={e => { e.target.style.borderColor = "#d9d9d9"; e.target.style.background = "#fafafa"; }}
                  />
                </div>
                <div>
                  <label style={{ display: "block", fontSize: 14, fontWeight: 500, color: "#434343", marginBottom: 6 }}>确认密码</label>
                  <input type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)}
                    placeholder="请再次输入新密码"
                    style={{ width: "100%", height: 42, padding: "0 14px", border: "1px solid #d9d9d9", borderRadius: 8, fontSize: 14, outline: "none", background: "#fafafa", boxSizing: "border-box" }}
                    onFocus={e => { e.target.style.borderColor = "#1677ff"; e.target.style.background = "#fff"; }}
                    onBlur={e => { e.target.style.borderColor = "#d9d9d9"; e.target.style.background = "#fafafa"; }}
                  />
                </div>
              </div>

              <div style={{ display: "flex", justifyContent: "flex-end", gap: 12, marginTop: 24 }}>
                <button onClick={() => { setShowPasswordModal(false); setOldPassword(""); setNewPassword(""); setConfirmPassword(""); }}
                  style={{ height: 40, padding: "0 20px", fontSize: 14, border: "1px solid #d9d9d9", borderRadius: 8, background: "#fff", color: "#595959", cursor: "pointer" }}>
                  取消
                </button>
                <button onClick={handleChangePassword} disabled={passwordLoading}
                  style={{
                    height: 40, padding: "0 20px", fontSize: 14, border: "none", borderRadius: 8, cursor: passwordLoading ? "not-allowed" : "pointer",
                    color: "#fff", background: passwordLoading ? "#91caff" : "linear-gradient(135deg, #1677ff, #0958d9)",
                    boxShadow: passwordLoading ? "none" : "0 2px 8px rgba(22,119,255,0.3)", opacity: passwordLoading ? 0.6 : 1
                  }}>
                  {passwordLoading ? "修改中..." : "确认修改"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
