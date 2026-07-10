import { Card, Col, Row, Table, Tag, Tooltip, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { DateUtil } from "../utils/DateUtil";

interface RecentTaskRow {
  id: string;
  agentName: string;
  taskName: string;
  execStatusLabel: string;
  failureReason: string;
  updateTime: string;
}

const rows: RecentTaskRow[] = [
  { id: "1", agentName: "需求分析智能体", taskName: "聚合页面规划", execStatusLabel: "执行成功", failureReason: "", updateTime: "2026-05-16T10:00:00" },
  { id: "2", agentName: "命令调度智能体", taskName: "命令链路验证", execStatusLabel: "等待执行", failureReason: "等待调度", updateTime: "2026-05-16T10:20:00" }
];

const columns: ColumnsType<RecentTaskRow> = [
  { title: "智能体名称", dataIndex: "agentName" },
  { title: "任务名称", dataIndex: "taskName" },
  { title: "执行状态", dataIndex: "execStatusLabel", render: value => <Tag color={value === "执行成功" ? "green" : "blue"}>{value}</Tag> },
  { title: "失败原因/备注", dataIndex: "failureReason", ellipsis: true, render: value => <Tooltip title={value}>{value || "-"}</Tooltip> },
  { title: "更新时间", dataIndex: "updateTime", render: value => DateUtil.formatDateTime(value) }
];

export function AgentWorkbenchPage() {
  return (
    <div>
      <Typography.Title level={3}>智能体工作台</Typography.Title>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}><Card title="智能体数量">12</Card></Col>
        <Col span={6}><Card title="技能数量">38</Card></Col>
        <Col span={6}><Card title="运行任务">7</Card></Col>
        <Col span={6}><Card title="失败待排查">2</Card></Col>
      </Row>
      <Table rowKey="id" bordered columns={columns} dataSource={rows} pagination={false} />
    </div>
  );
}
