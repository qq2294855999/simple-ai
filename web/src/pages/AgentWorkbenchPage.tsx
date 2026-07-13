import { Button, Card, Col, Row, Table, Tag, Tooltip, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useCallback, useEffect, useState } from "react";
import { AgentDashboardApi } from "../api/agentDashboardApi";
import type { AgentDashboardRecentTaskDto, AgentDashboardSummaryDto } from "../dto/agentDashboard/AgentDashboardDto";
import { DateUtil } from "../utils/DateUtil";

const emptySummary: AgentDashboardSummaryDto = {
  agentCount: 0,
  enabledAgentCount: 0,
  skillCount: 0,
  runningTaskCount: 0,
  failedTaskCount: 0
};

/**
 * 智能体工作台页面。
 *
 * @author qty
 */
export function AgentWorkbenchPage() {
  const [summary, setSummary] = useState<AgentDashboardSummaryDto>(emptySummary);
  const [tasks, setTasks] = useState<AgentDashboardRecentTaskDto[]>([]);
  const [loading, setLoading] = useState(false);

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    try {
      const [summaryResult, taskResult] = await Promise.all([
        AgentDashboardApi.summary(),
        AgentDashboardApi.recentTasks()
      ]);
      setSummary(summaryResult);
      setTasks(taskResult);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadDashboard();
  }, [loadDashboard]);

  const columns: ColumnsType<AgentDashboardRecentTaskDto> = [
    { title: "智能体名称", dataIndex: "agentName", align: "center" },
    { title: "任务名称", dataIndex: "taskName", align: "center", ellipsis: true, render: value => <Tooltip title={value}>{value}</Tooltip> },
    { title: "执行状态", dataIndex: "execStatusLabel", align: "center", width: 130, render: (value, record) => <Tag color={getStatusColor(record.execStatus)}>{value}</Tag> },
    { title: "失败原因/备注", dataIndex: "failureReason", align: "center", ellipsis: true, render: value => <Tooltip title={value}>{value || "-"}</Tooltip> },
    { title: "更新时间", dataIndex: "updateTime", align: "center", width: 180, render: value => DateUtil.formatDateTime(value) }
  ];

  return (
    <div>
      <Typography.Title level={3}>智能体工作台</Typography.Title>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={5}><Card title="智能体数量" loading={loading}>{summary.agentCount}</Card></Col>
        <Col span={5}><Card title="启用智能体" loading={loading}>{summary.enabledAgentCount}</Card></Col>
        <Col span={5}><Card title="技能数量" loading={loading}>{summary.skillCount}</Card></Col>
        <Col span={5}><Card title="运行任务" loading={loading}>{summary.runningTaskCount}</Card></Col>
        <Col span={4}><Card title="失败待排查" loading={loading}>{summary.failedTaskCount}</Card></Col>
      </Row>
      <div className="simple-toolbar">
        <Typography.Text>近期任务</Typography.Text>
        <Button onClick={() => void loadDashboard()} loading={loading}>刷新</Button>
      </div>
      <Table rowKey="id" bordered loading={loading} columns={columns} dataSource={tasks} pagination={false} />
    </div>
  );
}

/**
 * 获取任务状态标签颜色。
 *
 * @param execStatus 执行状态
 * @return 标签颜色
 */
function getStatusColor(execStatus: string) {
  if (execStatus === "SUCCESS") {
    return "green";
  }
  if (execStatus === "FAILED") {
    return "red";
  }
  if (execStatus === "RUNNING") {
    return "blue";
  }
  return "default";
}
