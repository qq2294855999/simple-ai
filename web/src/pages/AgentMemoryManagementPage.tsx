import { Button, Form, Input, Modal, Select, Space, Table, Tooltip, Typography, Popconfirm, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { usePreventDoubleClickHook } from "../hooks/usePreventDoubleClickHook";
import { ToastUtil } from "../utils/ToastUtil";
import { AgentMemoryApi } from "../api/agentMemoryApi";
import { AgentDefinitionApi } from "../api/agentDefinitionApi";
import type { AgentMemoryPageResponseDto } from "../dto/agentMemory/AgentMemoryDto";
import type { CreateAgentMemoryRequestDto, UpdateAgentMemoryRequestDto } from "../dto/agentMemory/AgentMemoryDto";

/**
 * 获取状态中文标签。
 */
function getStatusLabel(status: string): string {
  if (status === "ENABLE") return "启用";
  if (status === "DISABLE") return "停用";
  return status || "-";
}

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
 * 智能体记忆编排管理页面。
 *
 * @author qty
 */
export function AgentMemoryManagementPage() {
  const [dataSource, setDataSource] = useState<AgentMemoryPageResponseDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

  const [keyword, setKeyword] = useState("");
  const [filterAgentId, setFilterAgentId] = useState<string | undefined>(undefined);
  const [filterStatus, setFilterStatus] = useState<string | undefined>(undefined);
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
      const result = await AgentMemoryApi.page({
        current: pageIndex, size: pageSize,
        keyword: keyword || undefined, agentId: filterAgentId, status: filterStatus
      });
      setDataSource(result.records || []);
      setTotal(result.total || 0);
    } finally { setLoading(false); }
  }, [pageIndex, pageSize, keyword, filterAgentId, filterStatus]);

  loadDataRef.current = loadData;

  useEffect(() => { loadData(); loadAgents(); }, [loadData, loadAgents]);

  const handleSearch = useCallback(() => { setPageIndex(1); loadDataRef.current?.(); }, []);
  const handleReset = useCallback(() => {
    setKeyword(""); setFilterAgentId(undefined); setFilterStatus(undefined); setPageIndex(1);
    setTimeout(() => loadDataRef.current?.(), 0);
  }, []);

  const openCreateModal = useCallback(() => { setEditingId(null); form.resetFields(); setShowModal(true); }, [form]);
  const openEditModal = useCallback(async (id: string) => {
    setEditingId(id);
    try {
      const record = await AgentMemoryApi.findOne(id);
      form.setFieldsValue({
        agentId: record.agentId, memoryName: record.memoryName, stepName: record.stepName,
        triggerCondition: record.triggerCondition, triggerAction: record.triggerAction, remark: record.remark
      });
    } catch { ToastUtil.error("获取详情失败"); }
    setShowModal(true);
  }, [form]);

  const { onClick: handleSubmit, loading: submitLoading } = usePreventDoubleClickHook(async () => {
    const values = await form.validateFields();
    if (editingId) {
      await AgentMemoryApi.update(editingId, { id: editingId, ...values } as UpdateAgentMemoryRequestDto);
      ToastUtil.success("更新成功");
    } else {
      await AgentMemoryApi.create(values as CreateAgentMemoryRequestDto);
      ToastUtil.success("创建成功");
    }
    setShowModal(false);
    loadDataRef.current?.();
  });

  const { onClick: handleBatchDelete, loading: batchDeleteLoading } = usePreventDoubleClickHook(async () => {
    if (selectedRowKeys.length === 0) { ToastUtil.error("请先选择要删除的记录"); return; }
    await AgentMemoryApi.deleteByIds(selectedRowKeys);
    ToastUtil.success("删除成功");
    setSelectedRowKeys([]);
    loadDataRef.current?.();
  });

  const handleDelete = useCallback(async (id: string) => {
    await AgentMemoryApi.deleteByIds([id]);
    ToastUtil.success("删除成功");
    loadDataRef.current?.();
  }, []);

  const columns = useMemo<ColumnsType<AgentMemoryPageResponseDto>>(() => [
    { title: "记忆名称", dataIndex: "memoryName", width: 140 },
    { title: "所属智能体", dataIndex: "agentName", width: 140 },
    { title: "步骤名称", dataIndex: "stepName", width: 140, ellipsis: true, render: v => <Tooltip title={v}>{v}</Tooltip> },
    { title: "触发条件", dataIndex: "triggerCondition", width: 180, ellipsis: true, render: v => <Tooltip title={v}>{v}</Tooltip> },
    { title: "触发动作", dataIndex: "triggerAction", width: 180, ellipsis: true, render: v => <Tooltip title={v}>{v}</Tooltip> },
    { title: "步骤数", dataIndex: "stepCount", width: 80 },
    { title: "任务数", dataIndex: "taskCount", width: 80 },
    { title: "最近任务", dataIndex: "latestTaskStatusLabel", width: 100, render: (v: string, r: AgentMemoryPageResponseDto) => <Tag color={getExecStatusColor(r.latestTaskStatus)}>{v || "-"}</Tag> },
    { title: "状态", dataIndex: "status", width: 80, render: (s: string) => <Tooltip title={getStatusLabel(s)}>{getStatusLabel(s)}</Tooltip> },
    { title: "备注", dataIndex: "remark", ellipsis: true, render: v => <Tooltip title={v}>{v || "-"}</Tooltip> },
    { title: "更新时间", dataIndex: "updateTime", width: 160 },
    { title: "操作", width: 150,
      render: (_: unknown, record: AgentMemoryPageResponseDto) => (
        <Space>
          <Button type="link" onClick={() => openEditModal(record.id)}>编辑</Button>
          <Popconfirm title="确定删除该记忆吗？" onConfirm={() => handleDelete(record.id)} okButtonProps={{ danger: true }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ], [openEditModal, handleDelete]);

  return (
    <div>
      <Typography.Title level={3}>记忆编排管理</Typography.Title>
      <div className="simple-search-panel">
        <Space wrap>
          <Input placeholder="关键字（记忆名称/智能体名称）" value={keyword} onChange={e => setKeyword(e.target.value)} style={{ width: 240, height: 36 }} allowClear />
          <Select placeholder="所属智能体" value={filterAgentId} onChange={setFilterAgentId} style={{ width: 160, height: 36 }} allowClear options={agents.map(a => ({ label: a.name, value: a.id }))} />
          <Select placeholder="状态" value={filterStatus} onChange={setFilterStatus} style={{ width: 120, height: 36 }} allowClear options={[{ label: "启用", value: "ENABLE" }, { label: "停用", value: "DISABLE" }]} />
          <Button type="primary" onClick={handleSearch}>搜索</Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
      </div>
      <div className="simple-toolbar">
        <Space><Button danger loading={batchDeleteLoading} onClick={handleBatchDelete} disabled={selectedRowKeys.length === 0}>批量删除</Button></Space>
        <Button type="primary" onClick={openCreateModal}>新增记忆</Button>
      </div>
      <Table rowKey="id" bordered columns={columns} dataSource={dataSource} loading={loading}
        rowSelection={{ selectedRowKeys, onChange: keys => setSelectedRowKeys(keys as string[]) }}
        pagination={{ current: pageIndex, pageSize, total, showSizeChanger: true, showTotal: t => `共 ${t} 条`, onChange: (p, s) => { setPageIndex(p); setPageSize(s); } }}
      />
      <Modal open={showModal} onCancel={() => setShowModal(false)} title={editingId ? "编辑记忆" : "新增记忆"} width={520} centered
        footer={[<Button key="cancel" onClick={() => setShowModal(false)}>取消</Button>, <Button key="confirm" type="primary" loading={submitLoading} onClick={handleSubmit}>{editingId ? "确认更新" : "确认创建"}</Button>]}>
        <div className="simple-form-container">
          <Form form={form} layout="horizontal" size="middle" labelCol={{ span: 5 }} wrapperCol={{ span: 19 }}>
            <Form.Item label="所属智能体" name="agentId" rules={[{ required: true, message: "请选择智能体" }]} style={{ marginBottom: 16 }}>
              <Select placeholder="选择智能体" style={{ height: 36 }} options={agents.map(a => ({ label: a.name, value: a.id }))} showSearch filterOption={(input, option) => (option?.label as string || "").includes(input)} />
            </Form.Item>
            <Form.Item label="记忆名称" name="memoryName" rules={[{ required: true, message: "请输入记忆名称" }]} style={{ marginBottom: 16 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="步骤名称" name="stepName" rules={[{ required: true, message: "请输入步骤名称" }]} style={{ marginBottom: 16 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="触发条件" name="triggerCondition" rules={[{ required: true, message: "请输入触发条件" }]} style={{ marginBottom: 16 }}>
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item label="触发动作" name="triggerAction" rules={[{ required: true, message: "请输入触发动作" }]} style={{ marginBottom: 16 }}>
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item label="备注" name="remark" style={{ marginBottom: 0 }}><Input.TextArea rows={3} /></Form.Item>
          </Form>
        </div>
      </Modal>
    </div>
  );
}
