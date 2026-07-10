import { Button, Form, Input, Modal, Select, Space, Table, Tooltip, Typography, Popconfirm } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { usePreventDoubleClickHook } from "../hooks/usePreventDoubleClickHook";
import { ToastUtil } from "../utils/ToastUtil";
import { SubAgentRelationApi } from "../api/subAgentRelationApi";
import { AgentDefinitionApi } from "../api/agentDefinitionApi";
import type { SubAgentRelationPageResponseDto } from "../dto/subAgentRelation/SubAgentRelationDto";
import type { CreateSubAgentRelationRequestDto, UpdateSubAgentRelationRequestDto } from "../dto/subAgentRelation/SubAgentRelationDto";

/**
 * 获取状态中文标签。
 */
function getStatusLabel(status: string): string {
  if (status === "ENABLE") return "启用";
  if (status === "DISABLE") return "停用";
  return status || "-";
}

/**
 * 子智能体编排管理页面。
 *
 * @author qty
 */
export function SubAgentRelationManagementPage() {
  const [dataSource, setDataSource] = useState<SubAgentRelationPageResponseDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

  const [keyword, setKeyword] = useState("");
  const [filterMainAgentId, setFilterMainAgentId] = useState<string | undefined>(undefined);
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
      const list = (result.records || [])
        .filter((a: { name?: string }) => a.name)
        .map((a: { id: string; name: string }) => ({ id: a.id, name: a.name }));
      setAgents(list);
    } catch {
      // ignore
    }
  }, []);

  const loadDataRef = useRef<(() => Promise<void>) | null>(null);
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await SubAgentRelationApi.page({
        current: pageIndex,
        size: pageSize,
        keyword: keyword || undefined,
        mainAgentId: filterMainAgentId,
        status: filterStatus
      });
      setDataSource(result.records || []);
      setTotal(result.total || 0);
    } finally {
      setLoading(false);
    }
  }, [pageIndex, pageSize, keyword, filterMainAgentId, filterStatus]);

  loadDataRef.current = loadData;

  useEffect(() => {
    loadData();
    loadAgents();
  }, [loadData, loadAgents]);

  const handleSearch = useCallback(() => {
    setPageIndex(1);
    loadDataRef.current?.();
  }, []);

  const handleReset = useCallback(() => {
    setKeyword("");
    setFilterMainAgentId(undefined);
    setFilterStatus(undefined);
    setPageIndex(1);
    setTimeout(() => loadDataRef.current?.(), 0);
  }, []);

  const openCreateModal = useCallback(() => {
    setEditingId(null);
    form.resetFields();
    setShowModal(true);
  }, [form]);

  const openEditModal = useCallback(async (id: string) => {
    setEditingId(id);
    try {
      const record = await SubAgentRelationApi.findOne(id);
      form.setFieldsValue({
        mainAgentId: record.mainAgentId,
        subAgentId: record.subAgentId,
        remark: record.remark
      });
    } catch {
      ToastUtil.error("获取详情失败");
    }
    setShowModal(true);
  }, [form]);

  const { onClick: handleSubmit, loading: submitLoading } = usePreventDoubleClickHook(async () => {
    const values = await form.validateFields();
    if (editingId) {
      const updateData: UpdateSubAgentRelationRequestDto = { id: editingId, ...values };
      await SubAgentRelationApi.update(editingId, updateData);
      ToastUtil.success("更新成功");
    } else {
      const createData: CreateSubAgentRelationRequestDto = values;
      await SubAgentRelationApi.create(createData);
      ToastUtil.success("创建成功");
    }
    setShowModal(false);
    loadDataRef.current?.();
  });

  const { onClick: handleBatchDelete, loading: batchDeleteLoading } = usePreventDoubleClickHook(async () => {
    if (selectedRowKeys.length === 0) {
      ToastUtil.error("请先选择要删除的记录");
      return;
    }
    await SubAgentRelationApi.deleteByIds(selectedRowKeys);
    ToastUtil.success("删除成功");
    setSelectedRowKeys([]);
    loadDataRef.current?.();
  });

  const handleDelete = useCallback(async (id: string) => {
    await SubAgentRelationApi.deleteByIds([id]);
    ToastUtil.success("删除成功");
    loadDataRef.current?.();
  }, []);

  const columns = useMemo<ColumnsType<SubAgentRelationPageResponseDto>>(() => [
    { title: "主智能体", dataIndex: "mainAgentName", width: 140 },
    { title: "子智能体", dataIndex: "subAgentName", width: 140 },
    { title: "状态", dataIndex: "status", width: 80, render: (s: string) => <Tooltip title={getStatusLabel(s)}>{getStatusLabel(s)}</Tooltip> },
    { title: "备注", dataIndex: "remark", ellipsis: true, render: v => <Tooltip title={v}>{v || "-"}</Tooltip> },
    { title: "修改时间", dataIndex: "updateTime", width: 160 },
    {
      title: "操作", width: 150,
      render: (_: unknown, record: SubAgentRelationPageResponseDto) => (
        <Space>
          <Button type="link" onClick={() => openEditModal(record.id)}>编辑</Button>
          <Popconfirm title="确定删除该关系吗？" onConfirm={() => handleDelete(record.id)} okButtonProps={{ danger: true }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ], [openEditModal, handleDelete]);

  return (
    <div>
      <Typography.Title level={3}>子智能体编排</Typography.Title>

      <div className="simple-search-panel">
        <Space wrap>
          <Input
            placeholder="关键字（主/子智能体名称）"
            value={keyword}
            onChange={e => setKeyword(e.target.value)}
            style={{ width: 220, height: 36 }}
            allowClear
          />
          <Select
            placeholder="主智能体"
            value={filterMainAgentId}
            onChange={setFilterMainAgentId}
            style={{ width: 160, height: 36 }}
            allowClear
            options={agents.map(a => ({ label: a.name, value: a.id }))}
          />
          <Select
            placeholder="状态"
            value={filterStatus}
            onChange={setFilterStatus}
            style={{ width: 120, height: 36 }}
            allowClear
            options={[{ label: "启用", value: "ENABLE" }, { label: "停用", value: "DISABLE" }]}
          />
          <Button type="primary" onClick={handleSearch}>搜索</Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
      </div>

      <div className="simple-toolbar">
        <Space>
          <Button danger loading={batchDeleteLoading} onClick={handleBatchDelete} disabled={selectedRowKeys.length === 0}>批量删除</Button>
        </Space>
        <Button type="primary" onClick={openCreateModal}>新增关系</Button>
      </div>

      <Table
        rowKey="id"
        bordered
        columns={columns}
        dataSource={dataSource}
        loading={loading}
        rowSelection={{ selectedRowKeys, onChange: keys => setSelectedRowKeys(keys as string[]) }}
        pagination={{
          current: pageIndex,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: t => `共 ${t} 条`,
          onChange: (p, s) => { setPageIndex(p); setPageSize(s); }
        }}
      />

      <Modal
        open={showModal}
        onCancel={() => setShowModal(false)}
        title={editingId ? "编辑关系" : "新增关系"}
        width={520}
        centered
        footer={[
          <Button key="cancel" onClick={() => setShowModal(false)}>取消</Button>,
          <Button key="confirm" type="primary" loading={submitLoading} onClick={handleSubmit}>
            {editingId ? "确认更新" : "确认创建"}
          </Button>
        ]}
      >
        <div className="simple-form-container">
          <Form form={form} layout="horizontal" size="middle" labelCol={{ span: 5 }} wrapperCol={{ span: 19 }}>
            <Form.Item
              label="主智能体"
              name="mainAgentId"
              rules={[{ required: true, message: "请选择主智能体" }]}
              style={{ marginBottom: 16 }}
            >
              <Select
                placeholder="选择主智能体"
                style={{ height: 36 }}
                options={agents.map(a => ({ label: a.name, value: a.id }))}
                showSearch
                filterOption={(input, option) => (option?.label as string || "").includes(input)}
              />
            </Form.Item>
            <Form.Item
              label="子智能体"
              name="subAgentId"
              rules={[{ required: true, message: "请选择子智能体" }]}
              style={{ marginBottom: 16 }}
            >
              <Select
                placeholder="选择子智能体"
                style={{ height: 36 }}
                options={agents.map(a => ({ label: a.name, value: a.id }))}
                showSearch
                filterOption={(input, option) => (option?.label as string || "").includes(input)}
              />
            </Form.Item>
            <Form.Item label="备注" name="remark" style={{ marginBottom: 0 }}>
              <Input.TextArea rows={3} />
            </Form.Item>
          </Form>
        </div>
      </Modal>
    </div>
  );
}
