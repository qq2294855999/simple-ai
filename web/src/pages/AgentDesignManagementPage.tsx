import { Button, Form, Input, Modal, Select, Space, Table, Tooltip, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useCallback, useMemo, useState } from "react";
import { usePreventDoubleClickHook } from "../hooks/usePreventDoubleClickHook";
import { ToastUtil } from "../utils/ToastUtil";

interface AgentRow {
  id: string;
  name: string;
  model: string;
  statusLabel: string;
  skillCount: number;
  ruleCount: number;
  memoryCount: number;
  recentTaskStatusLabel: string;
  remark: string;
}

const agentRows: AgentRow[] = [
  { id: "1", name: "需求分析智能体", model: "qwen-plus", statusLabel: "启用", skillCount: 6, ruleCount: 3, memoryCount: 4, recentTaskStatusLabel: "执行成功", remark: "负责需求拆解、流程绘制与开发计划沉淀" },
  { id: "2", name: "命令调度智能体", model: "qwen-max", statusLabel: "启用", skillCount: 4, ruleCount: 2, memoryCount: 3, recentTaskStatusLabel: "等待执行", remark: "负责原子命令编排与执行进度追踪" }
];

const modelOptions = [
  { label: "qwen-plus", value: "qwen-plus" },
  { label: "qwen-max", value: "qwen-max" }
];

export function AgentDesignManagementPage() {
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm();
  const statusValue = Form.useWatch("status", form);

  const openCreateModal = useCallback(() => {
    form.resetFields();
    setOpen(true);
  }, [form]);

  const { onClick: handleSubmit, loading } = usePreventDoubleClickHook(async () => {
    await form.validateFields();
    ToastUtil.success("骨架提交验证成功，后续接入后端接口");
    setOpen(false);
  });

  const columns = useMemo<ColumnsType<AgentRow>>(() => [
    { title: "智能体名称", dataIndex: "name" },
    { title: "模型", dataIndex: "model" },
    { title: "状态", dataIndex: "statusLabel" },
    { title: "技能数", dataIndex: "skillCount" },
    { title: "规则数", dataIndex: "ruleCount" },
    { title: "记忆数", dataIndex: "memoryCount" },
    { title: "最近任务状态", dataIndex: "recentTaskStatusLabel" },
    { title: "备注", dataIndex: "remark", ellipsis: true, render: value => <Tooltip title={value}>{value}</Tooltip> },
    { title: "操作", width: 150, render: () => <Space><Button type="link">详情</Button><Button type="link" danger>删除</Button></Space> }
  ], []);

  return (
    <div>
      <Typography.Title level={3}>智能体设计管理</Typography.Title>
      <div className="simple-search-panel">
        <Space wrap>
          <Input placeholder="智能体名称/描述" style={{ width: 200, height: 36 }} />
          <Select placeholder="模型" options={modelOptions} style={{ width: 120, height: 36 }} />
          <Select placeholder="状态" options={[{ label: "启用", value: "ENABLE" }, { label: "停用", value: "DISABLE" }]} style={{ width: 120, height: 36 }} />
          <Button type="primary">搜索</Button>
          <Button>重置</Button>
        </Space>
      </div>
      <div className="simple-toolbar">
        <Space><Button>全选</Button><Button danger>批量删除</Button></Space>
        <Button type="primary" onClick={openCreateModal}>新增智能体</Button>
      </div>
      <Table rowKey="id" bordered columns={columns} dataSource={agentRows} />
      <Modal
        open={open}
        onCancel={() => setOpen(false)}
        title="新增智能体"
        width={520}
        centered
        footer={[<Button key="cancel" onClick={() => setOpen(false)}>取消</Button>, <Button key="confirm" type="primary" loading={loading} onClick={handleSubmit}>确认创建</Button>]}
      >
        <div className="simple-form-container">
          <Form form={form} layout="horizontal" size="middle" labelCol={{ span: 5 }} wrapperCol={{ span: 19 }}>
            <Form.Item label="名称" name="name" rules={[{ required: true, message: "请输入名称" }]} style={{ marginBottom: 16 }}><Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} /></Form.Item>
            <Form.Item label="模型" name="model" rules={[{ required: true, message: "请选择模型" }]} style={{ marginBottom: 16 }}><Select options={modelOptions} style={{ height: 36 }} /></Form.Item>
            <Form.Item label="状态" name="status" style={{ marginBottom: 16 }}><Select options={[{ label: "启用", value: "ENABLE" }, { label: "停用", value: "DISABLE" }]} style={{ height: 36 }} /></Form.Item>
            <Form.Item label="描述" name="definitionDesc" rules={[{ required: true, message: "请输入描述" }]} style={{ marginBottom: 16 }}><Input.TextArea rows={3} /></Form.Item>
            {statusValue === "DISABLE" && <Form.Item label="停用原因" name="disableReason" style={{ marginBottom: 16 }}><Input /></Form.Item>}
            <Form.Item label="备注" name="remark" style={{ marginBottom: 0 }}><Input.TextArea rows={3} /></Form.Item>
          </Form>
        </div>
      </Modal>
    </div>
  );
}
