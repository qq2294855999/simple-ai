import { Button, Form, Input, Select, Space, Table, Tooltip, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { usePreventDoubleClickHook } from "../hooks/usePreventDoubleClickHook";
import { ToastUtil } from "../utils/ToastUtil";

interface CommandRow {
  id: string;
  agentName: string;
  skillName: string;
  command: string;
  dispatchStatusLabel: string;
  remark: string;
}

const rows: CommandRow[] = [
  { id: "1", agentName: "命令调度智能体", skillName: "读取上下文", command: "READ_CONTEXT", dispatchStatusLabel: "可调度", remark: "展示名称字段，提交时传递智能体 ID 和技能 ID" }
];

const columns: ColumnsType<CommandRow> = [
  { title: "智能体名称", dataIndex: "agentName" },
  { title: "技能名称", dataIndex: "skillName" },
  { title: "命令", dataIndex: "command", ellipsis: true, render: value => <Tooltip title={value}>{value}</Tooltip> },
  { title: "调度状态", dataIndex: "dispatchStatusLabel" },
  { title: "备注", dataIndex: "remark", ellipsis: true, render: value => <Tooltip title={value}>{value}</Tooltip> },
  { title: "操作", width: 150, render: () => <Button type="link">查看进度</Button> }
];

export function CommandDispatchPage() {
  const [form] = Form.useForm();
  const { onClick: handleDispatch, loading } = usePreventDoubleClickHook(async () => {
    await form.validateFields();
    ToastUtil.success("骨架调度提交验证成功，后续接入 dispatch-stream");
  });

  return (
    <div>
      <Typography.Title level={3}>命令调度</Typography.Title>
      <div className="simple-search-panel">
        <Form form={form} layout="inline">
          <Form.Item name="agentId" rules={[{ required: true, message: "请选择智能体" }]}>
            <Select placeholder="选择智能体" style={{ width: 200, height: 36 }} options={[{ label: "命令调度智能体", value: "1" }]} />
          </Form.Item>
          <Form.Item name="skillId" rules={[{ required: true, message: "请选择技能" }]}>
            <Select placeholder="选择技能" style={{ width: 200, height: 36 }} options={[{ label: "读取上下文", value: "1" }]} />
          </Form.Item>
          <Form.Item name="command" rules={[{ required: true, message: "请输入命令" }]}>
            <Input placeholder="命令内容" style={{ width: 200, height: 36 }} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" loading={loading} onClick={handleDispatch}>提交调度</Button>
          </Form.Item>
        </Form>
      </div>
      <div className="simple-toolbar"><Space><Button>全选</Button></Space><Button type="primary">刷新进度</Button></div>
      <Table rowKey="id" bordered columns={columns} dataSource={rows} />
    </div>
  );
}
