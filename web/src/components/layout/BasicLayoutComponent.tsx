import { RobotOutlined, SendOutlined, SettingOutlined, ThunderboltOutlined, SafetyOutlined, ApartmentOutlined, DatabaseOutlined, CodeOutlined, OrderedListOutlined } from "@ant-design/icons";
import { Layout, Menu, Space, Typography } from "antd";
import { Outlet, useLocation, useNavigate } from "react-router-dom";

const { Header, Sider, Content } = Layout;

export function BasicLayoutComponent() {
  const navigate = useNavigate();
  const location = useLocation();

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
            style={{ height: "100%", borderRight: 0 }}
            onClick={item => navigate(item.key)}
            items={[
              { key: "/workbench", icon: <RobotOutlined />, label: "智能体工作台" },
              { key: "/agent-design", icon: <SettingOutlined />, label: "智能体设计管理" },
              { key: "/command-dispatch", icon: <SendOutlined />, label: "命令调度" },
              { key: "/agent-skill", icon: <ThunderboltOutlined />, label: "技能管理" },
              { key: "/agent-rule", icon: <SafetyOutlined />, label: "规则管理" },
              { key: "/sub-agent-relation", icon: <ApartmentOutlined />, label: "子智能体编排" },
              { key: "/agent-memory", icon: <DatabaseOutlined />, label: "记忆编排管理" },
              { key: "/atomic-command", icon: <CodeOutlined />, label: "原子命令管理" },
              { key: "/task", icon: <OrderedListOutlined />, label: "任务执行记录" }
            ]}
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
