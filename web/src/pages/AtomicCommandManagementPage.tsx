import { Button, Form, Input, Modal, Select, Space, Table, Tooltip, Typography, Popconfirm } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { usePreventDoubleClickHook } from "../hooks/usePreventDoubleClickHook";
import { ToastUtil } from "../utils/ToastUtil";
import { AtomicCommandApi } from "../api/atomicCommandApi";
import { AgentSkillApi } from "../api/agentSkillApi";
import type { AtomicCommandPageResponseDto } from "../dto/atomicCommand/AtomicCommandDto";
import type { CreateAtomicCommandRequestDto, UpdateAtomicCommandRequestDto } from "../dto/atomicCommand/AtomicCommandDto";

/**
 * 获取状态中文标签。
 */
function getStatusLabel(status: string): string {
  if (status === "ENABLE") return "启用";
  if (status === "DISABLE") return "停用";
  return status || "-";
}

/**
 * 原子命令管理页面。
 *
 * @author qty
 */
export function AtomicCommandManagementPage() {
  const [dataSource, setDataSource] = useState<AtomicCommandPageResponseDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

  const [keyword, setKeyword] = useState("");
  const [filterSkillId, setFilterSkillId] = useState<string | undefined>(undefined);
  const [filterStatus, setFilterStatus] = useState<string | undefined>(undefined);
  const [pageIndex, setPageIndex] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form] = Form.useForm();

  // 技能下拉数据
  const [skills, setSkills] = useState<{ id: string; label: string }[]>([]);

  const loadSkills = useCallback(async () => {
    try {
      const result = await AgentSkillApi.listAll();
      setSkills((result.records || []).map((s: { id: string; definitionDesc?: string; agentName?: string }) => ({
        id: s.id,
        label: `${s.definitionDesc || s.id} (${s.agentName || "-"})`
      })));
    } catch { /* ignore */ }
  }, []);

  const loadDataRef = useRef<(() => Promise<void>) | null>(null);
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await AtomicCommandApi.page({
        current: pageIndex, size: pageSize,
        keyword: keyword || undefined, skillId: filterSkillId, status: filterStatus
      });
      setDataSource(result.records || []);
      setTotal(result.total || 0);
    } finally { setLoading(false); }
  }, [pageIndex, pageSize, keyword, filterSkillId, filterStatus]);

  loadDataRef.current = loadData;

  useEffect(() => { loadData(); loadSkills(); }, [loadData, loadSkills]);

  const handleSearch = useCallback(() => { setPageIndex(1); loadDataRef.current?.(); }, []);
  const handleReset = useCallback(() => {
    setKeyword(""); setFilterSkillId(undefined); setFilterStatus(undefined); setPageIndex(1);
    setTimeout(() => loadDataRef.current?.(), 0);
  }, []);

  const openCreateModal = useCallback(() => { setEditingId(null); form.resetFields(); setShowModal(true); }, [form]);
  const openEditModal = useCallback(async (id: string) => {
    setEditingId(id);
    try {
      const record = await AtomicCommandApi.findOne(id);
      form.setFieldsValue({
        name: record.name, command: record.command, role: record.role,
        skillId: record.skillId || undefined, remark: record.remark
      });
    } catch { ToastUtil.error("获取详情失败"); }
    setShowModal(true);
  }, [form]);

  const { onClick: handleSubmit, loading: submitLoading } = usePreventDoubleClickHook(async () => {
    const values = await form.validateFields();
    if (editingId) {
      await AtomicCommandApi.update(editingId, { id: editingId, ...values } as UpdateAtomicCommandRequestDto);
      ToastUtil.success("更新成功");
    } else {
      await AtomicCommandApi.create(values as CreateAtomicCommandRequestDto);
      ToastUtil.success("创建成功");
    }
    setShowModal(false);
    loadDataRef.current?.();
  });

  const { onClick: handleBatchDelete, loading: batchDeleteLoading } = usePreventDoubleClickHook(async () => {
    if (selectedRowKeys.length === 0) { ToastUtil.error("请先选择要删除的记录"); return; }
    await AtomicCommandApi.deleteByIds(selectedRowKeys);
    ToastUtil.success("删除成功");
    setSelectedRowKeys([]);
    loadDataRef.current?.();
  });

  const handleDelete = useCallback(async (id: string) => {
    await AtomicCommandApi.deleteByIds([id]);
    ToastUtil.success("删除成功");
    loadDataRef.current?.();
  }, []);

  const columns = useMemo<ColumnsType<AtomicCommandPageResponseDto>>(() => [
    { title: "名称", dataIndex: "name", width: 140 },
    { title: "命令", dataIndex: "command", width: 160, ellipsis: true, render: v => <Tooltip title={v}>{v}</Tooltip> },
    { title: "作用", dataIndex: "role", width: 120, ellipsis: true, render: v => <Tooltip title={v}>{v}</Tooltip> },
    { title: "关联技能", dataIndex: "skillDesc", width: 160, ellipsis: true, render: v => <Tooltip title={v}>{v || "-"}</Tooltip> },
    { title: "所属智能体", dataIndex: "agentName", width: 140 },
    { title: "状态", dataIndex: "status", width: 80, render: (s: string) => <Tooltip title={getStatusLabel(s)}>{getStatusLabel(s)}</Tooltip> },
    { title: "备注", dataIndex: "remark", ellipsis: true, render: v => <Tooltip title={v}>{v || "-"}</Tooltip> },
    { title: "更新时间", dataIndex: "updateTime", width: 160 },
    { title: "操作", width: 150,
      render: (_: unknown, record: AtomicCommandPageResponseDto) => (
        <Space>
          <Button type="link" onClick={() => openEditModal(record.id)}>编辑</Button>
          <Popconfirm title="确定删除该命令吗？" onConfirm={() => handleDelete(record.id)} okButtonProps={{ danger: true }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ], [openEditModal, handleDelete]);

  return (
    <div>
      <Typography.Title level={3}>原子命令管理</Typography.Title>
      <div className="simple-search-panel">
        <Space wrap>
          <Input placeholder="关键字（名称/命令）" value={keyword} onChange={e => setKeyword(e.target.value)} style={{ width: 200, height: 36 }} allowClear />
          <Select placeholder="关联技能" value={filterSkillId} onChange={setFilterSkillId} style={{ width: 200, height: 36 }} allowClear
            options={skills.map(s => ({ label: s.label, value: s.id }))} showSearch filterOption={(input, option) => (option?.label as string || "").includes(input)} />
          <Select placeholder="状态" value={filterStatus} onChange={setFilterStatus} style={{ width: 120, height: 36 }} allowClear
            options={[{ label: "启用", value: "ENABLE" }, { label: "停用", value: "DISABLE" }]} />
          <Button type="primary" onClick={handleSearch}>搜索</Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
      </div>
      <div className="simple-toolbar">
        <Space><Button danger loading={batchDeleteLoading} onClick={handleBatchDelete} disabled={selectedRowKeys.length === 0}>批量删除</Button></Space>
        <Button type="primary" onClick={openCreateModal}>新增命令</Button>
      </div>
      <Table rowKey="id" bordered columns={columns} dataSource={dataSource} loading={loading}
        rowSelection={{ selectedRowKeys, onChange: keys => setSelectedRowKeys(keys as string[]) }}
        pagination={{ current: pageIndex, pageSize, total, showSizeChanger: true, showTotal: t => `共 ${t} 条`, onChange: (p, s) => { setPageIndex(p); setPageSize(s); } }}
      />
      <Modal open={showModal} onCancel={() => setShowModal(false)} title={editingId ? "编辑命令" : "新增命令"} width={520} centered
        footer={[<Button key="cancel" onClick={() => setShowModal(false)}>取消</Button>, <Button key="confirm" type="primary" loading={submitLoading} onClick={handleSubmit}>{editingId ? "确认更新" : "确认创建"}</Button>]}>
        <div className="simple-form-container">
          <Form form={form} layout="horizontal" size="middle" labelCol={{ span: 5 }} wrapperCol={{ span: 19 }}>
            <Form.Item label="名称" name="name" rules={[{ required: true, message: "请输入名称" }]} style={{ marginBottom: 16 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="命令" name="command" rules={[{ required: true, message: "请输入命令" }]} style={{ marginBottom: 16 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="作用" name="role" rules={[{ required: true, message: "请输入作用" }]} style={{ marginBottom: 16 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="关联技能" name="skillId" style={{ marginBottom: 16 }}>
              <Select placeholder="选择技能（可选）" style={{ height: 36 }} allowClear
                options={skills.map(s => ({ label: s.label, value: s.id }))} showSearch filterOption={(input, option) => (option?.label as string || "").includes(input)} />
            </Form.Item>
            <Form.Item label="备注" name="remark" style={{ marginBottom: 0 }}><Input.TextArea rows={3} /></Form.Item>
          </Form>
        </div>
      </Modal>
    </div>
  );
}
