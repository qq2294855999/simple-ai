import { RobotOutlined, SendOutlined, SettingOutlined, MessageOutlined, CloudServerOutlined } from "@ant-design/icons";
import { Layout, Menu, Space, Typography } from "antd";
import { useEffect, useMemo, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";

const { Header, Sider, Content } = Layout;

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
 * 全局布局组件，含顶部 Header、左侧可折叠分组菜单、右侧内容区。
 *
 * @author qty
 */
export function BasicLayoutComponent() {
  const navigate = useNavigate();
  const location = useLocation();

  // 当前展开的分组菜单 key
  const [openKeys, setOpenKeys] = useState<string[]>(() => {
    const parentKey = CHILD_TO_PARENT[location.pathname];
    return parentKey ? [parentKey] : [];
  });

  // 路由切换时自动展开当前页所属分组
  useEffect(() => {
    const parentKey = CHILD_TO_PARENT[location.pathname];
    if (parentKey) {
      setOpenKeys(prev => (prev.includes(parentKey) ? prev : [...prev, parentKey]));
    }
  }, [location.pathname]);

  // 菜单项定义，业务分组结构：
  //   智能体工作台 / 人机对话 → 独立一级
  //   智能体配置 → 智能体设计管理 + 子智能体编排 + 技能管理 + 规则管理 + 记忆编排管理
  //   命令与执行 → 命令调度 + 原子命令管理 + 任务执行记录
  //   大模型管理 → 模型供应商 + 模型管理
  const menuItems = useMemo(() => [
    { key: "/workbench", icon: <RobotOutlined />, label: "智能体工作台" },
    { key: "/agent-chat", icon: <MessageOutlined />, label: "人机对话" },
    {
      key: "group-agent-config",
      icon: <SettingOutlined />,
      label: "智能体配置",
      children: [
        { key: "/agent-design", label: "智能体设计管理" },
        { key: "/sub-agent-relation", label: "子智能体编排" },
        { key: "/agent-skill", label: "技能管理" },
        { key: "/agent-rule", label: "规则管理" },
        { key: "/agent-memory", label: "记忆编排管理" }
      ]
    },
    {
      key: "group-command",
      icon: <SendOutlined />,
      label: "命令与执行",
      children: [
        { key: "/command-dispatch", label: "命令调度" },
        { key: "/atomic-command", label: "原子命令管理" },
        { key: "/task", label: "任务执行记录" }
      ]
    },
    {
      key: "group-large-model",
      icon: <CloudServerOutlined />,
      label: "大模型管理",
      children: [
        { key: "/ai-model-provider", label: "模型供应商" },
        { key: "/ai-model", label: "模型管理" }
      ]
    }
  ], []);

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Header style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 24px", background: "#001529" }}>
        <Typography.Title level={4} style={{ color: "#fff", margin: 0 }}>Simple AI 管理端</Typography.Title>
        <Space style={{ color: "#fff" }}>业务骨架阶段：暂不介入登录鉴权</Space>
      </Header>
      <Layout>
        <Sider width={220} theme="light">
          <Menu
            mode="inline"
            selectedKeys={[location.pathname]}
            openKeys={openKeys}
            onOpenChange={setOpenKeys}
            style={{ height: "100%", borderRight: 0 }}
            onClick={item => navigate(item.key)}
            items={menuItems}
          />
        </Sider>
        <Layout style={{ padding: 24 }}>
          <Content style={{ padding: 24, margin: 0, minHeight: 280, background: "#fff", borderRadius: 8 }}>
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
}
