import { Button, Form, Input, Modal, Select, Space, Table, Tooltip, Typography, Popconfirm, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { usePreventDoubleClickHook } from "../hooks/usePreventDoubleClickHook";
import { ToastUtil } from "../utils/ToastUtil";
import { TaskApi } from "../api/taskApi";
import { AgentDefinitionApi } from "../api/agentDefinitionApi";
import type { TaskPageResponseDto } from "../dto/task/TaskDto";
import type { CreateTaskRequestDto, UpdateTaskRequestDto } from "../dto/task/TaskDto";

/**
 * 步骤类型枚举标签映射。
 */
const stepTypeLabels: Record<string, string> = {
  JUDGE: "判断",
  ATOMIC_COMMAND: "原子命令",
  LOOP_START: "循环开始",
  LOOP_END: "循环结束"
};

/**
 * 执行状态枚举标签映射。
 */
const execStatusLabels: Record<string, string> = {
  WAITING: "等待执行",
  RUNNING: "执行中",
  SUCCESS: "执行成功",
  FAILED: "执行失败"
};

/**
 * 获取执行状态颜色。
 */
function getExecStatusColor(status: string): string {
  if (status === "SUCCESS") return "green";
  if (status === "FAILED") return "red";
  if (status === "RUNNING") return "blue";
  return "default";
}

/**
 * 获取状态中文标签。
 */
function getStatusLabel(status: string): string {
  if (status === "ENABLE") return "启用";
  if (status === "DISABLE") return "停用";
  return status || "-";
}

/**
 * 任务执行记录管理页面。
 *
 * @author qty
 */
export function TaskManagementPage() {
  const [dataSource, setDataSource] = useState<TaskPageResponseDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

  const [keyword, setKeyword] = useState("");
  const [filterAgentId, setFilterAgentId] = useState<string | undefined>(undefined);
  const [filterExecStatus, setFilterExecStatus] = useState<string | undefined>(undefined);
  const [pageIndex, setPageIndex] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form] = Form.useForm();

  const [agents, setAgents] = useState<{ id: string; name: string }[]>([]);

  const loadAgents = useCallback(async () => {
    try {
      const result = await AgentDefinitionApi.listAll();
      setAgents((result.records || []).filter((a: { name?: string }) => a.name).map((a: { id: string; name: string }) => ({ id: a.id, name: a.name })));
    } catch { /* ignore */ }
  }, []);

  const loadDataRef = useRef<(() => Promise<void>) | null>(null);
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await TaskApi.page({
        current: pageIndex, size: pageSize,
        keyword: keyword || undefined, agentId: filterAgentId, execStatus: filterExecStatus
      });
      setDataSource(result.records || []);
      setTotal(result.total || 0);
    } finally { setLoading(false); }
  }, [pageIndex, pageSize, keyword, filterAgentId, filterExecStatus]);

  loadDataRef.current = loadData;

  useEffect(() => { loadData(); loadAgents(); }, [loadData, loadAgents]);

  const handleSearch = useCallback(() => { setPageIndex(1); loadDataRef.current?.(); }, []);
  const handleReset = useCallback(() => {
    setKeyword(""); setFilterAgentId(undefined); setFilterExecStatus(undefined); setPageIndex(1);
    setTimeout(() => loadDataRef.current?.(), 0);
  }, []);

  const openCreateModal = useCallback(() => { setEditingId(null); form.resetFields(); setShowModal(true); }, [form]);
  const openEditModal = useCallback(async (id: string) => {
    setEditingId(id);
    try {
      const record = await TaskApi.findOne(id);
      form.setFieldsValue({
        agentMemoryId: record.agentMemoryId, taskName: record.taskName,
        parentTaskId: record.parentTaskId || undefined, nextTaskId: record.nextTaskId || undefined,
        stepType: record.stepType, branchCondition: record.branchCondition, branchRoute: record.branchRoute, remark: record.remark
      });
    } catch { ToastUtil.error("获取详情失败"); }
    setShowModal(true);
  }, [form]);

  const { onClick: handleSubmit, loading: submitLoading } = usePreventDoubleClickHook(async () => {
    const values = await form.validateFields();
    if (editingId) {
      await TaskApi.update(editingId, { id: editingId, ...values } as UpdateTaskRequestDto);
      ToastUtil.success("更新成功");
    } else {
      await TaskApi.create(values as CreateTaskRequestDto);
      ToastUtil.success("创建成功");
    }
    setShowModal(false);
    loadDataRef.current?.();
  });

  const { onClick: handleBatchDelete, loading: batchDeleteLoading } = usePreventDoubleClickHook(async () => {
    if (selectedRowKeys.length === 0) { ToastUtil.error("请先选择要删除的记录"); return; }
    await TaskApi.deleteByIds(selectedRowKeys);
    ToastUtil.success("删除成功");
    setSelectedRowKeys([]);
    loadDataRef.current?.();
  });

  const handleDelete = useCallback(async (id: string) => {
    await TaskApi.deleteByIds([id]);
    ToastUtil.success("删除成功");
    loadDataRef.current?.();
  }, []);

  const columns = useMemo<ColumnsType<TaskPageResponseDto>>(() => [
    { title: "任务名称", dataIndex: "taskName", width: 140 },
    { title: "所属智能体", dataIndex: "agentName", width: 140 },
    { title: "记忆名称", dataIndex: "memoryName", width: 140 },
    { title: "父任务", dataIndex: "parentTaskName", width: 120, render: v => v || "-" },
    { title: "下一步任务", dataIndex: "nextTaskName", width: 120, render: v => v || "-" },
    { title: "步骤类型", dataIndex: "stepType", width: 100, render: (v: string) => <Tooltip title={stepTypeLabels[v] || v}>{stepTypeLabels[v] || v}</Tooltip> },
    { title: "执行状态", dataIndex: "execStatus", width: 100, render: (v: string) => <Tag color={getExecStatusColor(v)}>{execStatusLabels[v] || v || "-"}</Tag> },
    { title: "失败原因", dataIndex: "failureReason", width: 150, ellipsis: true, render: v => <Tooltip title={v}>{v || "-"}</Tooltip> },
    { title: "备注", dataIndex: "remark", ellipsis: true, render: v => <Tooltip title={v}>{v || "-"}</Tooltip> },
    { title: "创建时间", dataIndex: "createTime", width: 160 },
    { title: "操作", width: 150,
      render: (_: unknown, record: TaskPageResponseDto) => (
        <Space>
          <Button type="link" onClick={() => openEditModal(record.id)}>编辑</Button>
          <Popconfirm title="确定删除该任务吗？" onConfirm={() => handleDelete(record.id)} okButtonProps={{ danger: true }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ], [openEditModal, handleDelete]);

  return (
    <div>
      <Typography.Title level={3}>任务执行记录</Typography.Title>
      <div className="simple-search-panel">
        <Space wrap>
          <Input placeholder="关键字（任务名称/记忆名称）" value={keyword} onChange={e => setKeyword(e.target.value)} style={{ width: 240, height: 36 }} allowClear />
          <Select placeholder="所属智能体" value={filterAgentId} onChange={setFilterAgentId} style={{ width: 160, height: 36 }} allowClear options={agents.map(a => ({ label: a.name, value: a.id }))} />
          <Select placeholder="执行状态" value={filterExecStatus} onChange={setFilterExecStatus} style={{ width: 140, height: 36 }} allowClear
            options={Object.entries(execStatusLabels).map(([k, v]) => ({ label: v, value: k }))} />
          <Button type="primary" onClick={handleSearch}>搜索</Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
      </div>
      <div className="simple-toolbar">
        <Space><Button danger loading={batchDeleteLoading} onClick={handleBatchDelete} disabled={selectedRowKeys.length === 0}>批量删除</Button></Space>
        <Button type="primary" onClick={openCreateModal}>新增任务</Button>
      </div>
      <Table rowKey="id" bordered columns={columns} dataSource={dataSource} loading={loading}
        rowSelection={{ selectedRowKeys, onChange: keys => setSelectedRowKeys(keys as string[]) }}
        pagination={{ current: pageIndex, pageSize, total, showSizeChanger: true, showTotal: t => `共 ${t} 条`, onChange: (p, s) => { setPageIndex(p); setPageSize(s); } }}
      />
      <Modal open={showModal} onCancel={() => setShowModal(false)} title={editingId ? "编辑任务" : "新增任务"} width={520} centered
        footer={[<Button key="cancel" onClick={() => setShowModal(false)}>取消</Button>, <Button key="confirm" type="primary" loading={submitLoading} onClick={handleSubmit}>{editingId ? "确认更新" : "确认创建"}</Button>]}>
        <div className="simple-form-container">
          <Form form={form} layout="horizontal" size="middle" labelCol={{ span: 5 }} wrapperCol={{ span: 19 }}>
            <Form.Item label="记忆ID" name="agentMemoryId" rules={[{ required: true, message: "请输入记忆ID" }]} style={{ marginBottom: 16 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="任务名称" name="taskName" rules={[{ required: true, message: "请输入任务名称" }]} style={{ marginBottom: 16 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="步骤类型" name="stepType" rules={[{ required: true, message: "请选择步骤类型" }]} style={{ marginBottom: 16 }}>
              <Select style={{ height: 36 }} options={Object.entries(stepTypeLabels).map(([k, v]) => ({ label: v, value: k }))} />
            </Form.Item>
            <Form.Item label="父任务ID" name="parentTaskId" style={{ marginBottom: 16 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="下一步ID" name="nextTaskId" style={{ marginBottom: 16 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="分支条件" name="branchCondition" style={{ marginBottom: 16 }}><Input.TextArea rows={2} /></Form.Item>
            <Form.Item label="分支路由" name="branchRoute" style={{ marginBottom: 16 }}><Input.TextArea rows={2} /></Form.Item>
            <Form.Item label="备注" name="remark" style={{ marginBottom: 0 }}><Input.TextArea rows={3} /></Form.Item>
          </Form>
        </div>
      </Modal>
    </div>
  );
}
