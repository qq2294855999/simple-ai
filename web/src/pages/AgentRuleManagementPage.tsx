import { Button, Dropdown, Form, Input, Modal, Select, Space, Table, Tooltip, Typography, Popconfirm, Tag } from "antd";
import type { MenuProps } from "antd";
import type { ColumnsType } from "antd/es/table";
import { MoreOutlined } from "@ant-design/icons";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { usePreventDoubleClickHook } from "../hooks/usePreventDoubleClickHook";
import { ToastUtil } from "../utils/ToastUtil";
import { AgentRuleApi } from "../api/agentRuleApi";
import { AgentDefinitionApi } from "../api/agentDefinitionApi";
import type { AgentRulePageResponseDto } from "../dto/agentRule/AgentRuleDto";
import type { CreateAgentRuleRequestDto, UpdateAgentRuleRequestDto } from "../dto/agentRule/AgentRuleDto";

/**
 * 获取状态中文标签。
 *
 * @param status 状态码或状态名
 * @returns 状态中文
 */
function getStatusLabel(status: number | string): string {
  if (status === 1 || status === "ON") return "启用";
  if (status === 2 || status === "OFF") return "停用";
  return String(status) || "-";
}

/**
 * 智能体规则管理页面。
 *
 * @author qty
 */
export function AgentRuleManagementPage() {
  const [dataSource, setDataSource] = useState<AgentRulePageResponseDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

  // 搜索条件
  const [keyword, setKeyword] = useState("");
  const [filterAgentId, setFilterAgentId] = useState<string | undefined>(undefined);
  const [filterStatus, setFilterStatus] = useState<string | undefined>(undefined);
  const [pageIndex, setPageIndex] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  // 弹窗状态
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form] = Form.useForm();

  // 智能体下拉数据
  const [agents, setAgents] = useState<{ id: string; name: string }[]>([]);

  // 加载智能体下拉
  const loadAgents = useCallback(async () => {
    try {
      const result = await AgentDefinitionApi.listAll();
      const list = (result.records || [])
        .filter((a: { name?: string }) => a.name)
        .map((a: { id: string; name: string }) => ({ id: a.id, name: a.name }));
      setAgents(list);
    } catch {
      // 下拉加载失败不影响主流程
    }
  }, []);

  // 加载列表数据
  const loadDataRef = useRef<(() => Promise<void>) | null>(null);
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await AgentRuleApi.page({
        current: pageIndex,
        size: pageSize,
        keyword: keyword || undefined,
        agentId: filterAgentId,
        status: filterStatus
      });
      setDataSource(result.records || []);
      setTotal(result.total || 0);
    } finally {
      setLoading(false);
    }
  }, [pageIndex, pageSize, keyword, filterAgentId, filterStatus]);

  // 保持 loadData 引用最新
  loadDataRef.current = loadData;

  useEffect(() => {
    loadData();
    loadAgents();
  }, [loadData, loadAgents]);

  // 搜索
  const handleSearch = useCallback(() => {
    setPageIndex(1);
    loadDataRef.current?.();
  }, []);

  // 重置搜索
  const handleReset = useCallback(() => {
    setKeyword("");
    setFilterAgentId(undefined);
    setFilterStatus(undefined);
    setPageIndex(1);
    setTimeout(() => loadDataRef.current?.(), 0);
  }, []);

  // 打开创建弹窗
  const openCreateModal = useCallback(() => {
    setEditingId(null);
    form.resetFields();
    setShowModal(true);
  }, [form]);

  // 打开编辑弹窗
  const openEditModal = useCallback(async (id: string) => {
    setEditingId(id);
    try {
      const record = await AgentRuleApi.findOne(id);
      form.setFieldsValue({
        agentId: record.agentId,
        definitionDesc: record.definitionDesc,
        triggerCondition: record.triggerCondition,
        triggerAction: record.triggerAction,
        remark: record.remark
      });
    } catch {
      ToastUtil.error("获取详情失败");
    }
    setShowModal(true);
  }, [form]);

  // 提交创建/编辑
  const { onClick: handleSubmit, loading: submitLoading } = usePreventDoubleClickHook(async () => {
    const values = await form.validateFields();
    if (editingId) {
      const updateData: UpdateAgentRuleRequestDto = { id: editingId, ...values };
      await AgentRuleApi.update(editingId, updateData);
      ToastUtil.success("更新成功");
    } else {
      const createData: CreateAgentRuleRequestDto = values;
      await AgentRuleApi.create(createData);
      ToastUtil.success("创建成功");
    }
    setShowModal(false);
    loadDataRef.current?.();
  });

  // 批量删除
  const { onClick: handleBatchDelete, loading: batchDeleteLoading } = usePreventDoubleClickHook(async () => {
    if (selectedRowKeys.length === 0) {
      ToastUtil.error("请先选择要删除的记录");
      return;
    }
    await AgentRuleApi.deleteByIds(selectedRowKeys);
    ToastUtil.success("删除成功");
    setSelectedRowKeys([]);
    loadDataRef.current?.();
  });

  // 删除单个
  const handleDelete = useCallback(async (id: string) => {
    await AgentRuleApi.deleteByIds([id]);
    ToastUtil.success("删除成功");
    loadDataRef.current?.();
  }, []);

  // 启用
  const handleEnable = useCallback(async (id: string) => {
    await AgentRuleApi.enable(id);
    ToastUtil.success("已启用");
    loadDataRef.current?.();
  }, []);

  // 禁用
  const handleDisable = useCallback(async (id: string) => {
    await AgentRuleApi.disable(id);
    ToastUtil.success("已禁用");
    loadDataRef.current?.();
  }, []);

  // 表格列定义
  const columns = useMemo<ColumnsType<AgentRulePageResponseDto>>(() => [
    { title: "智能体名称", dataIndex: "agentName", width: 140 },
    { title: "定义描述", dataIndex: "definitionDesc", width: 200, ellipsis: true, render: value => <Tooltip title={value}>{value}</Tooltip> },
    { title: "触发条件", dataIndex: "triggerCondition", width: 200, ellipsis: true, render: value => <Tooltip title={value}>{value}</Tooltip> },
    { title: "触发动作", dataIndex: "triggerAction", width: 200, ellipsis: true, render: value => <Tooltip title={value}>{value}</Tooltip> },
    { title: "状态", dataIndex: "status", width: 80, render: (status: string) => <Tag color={getStatusLabel(status) === "启用" ? "green" : "red"}>{getStatusLabel(status)}</Tag> },
    { title: "备注", dataIndex: "remark", ellipsis: true, render: value => <Tooltip title={value}>{value || "-"}</Tooltip> },
    { title: "修改时间", dataIndex: "updateTime", width: 160 },
    {
      title: "操作", width: 150,
      render: (_: unknown, record: AgentRulePageResponseDto) => {
        const isEnabled = getStatusLabel(record.status) === "启用";
        const menuItems: MenuProps["items"] = [
          { key: "toggle", label: isEnabled ? "停用" : "启用" },
          { key: "delete", label: "删除", danger: true }
        ];
        const handleMenuAction = (key: string) => {
          if (key === "toggle") { if (isEnabled) { void handleDisable(record.id); } else { void handleEnable(record.id); } }
          if (key === "delete") { void handleDelete(record.id); }
        };
        return (
          <Space>
            <Button type="link" size="small" onClick={() => openEditModal(record.id)}>编辑</Button>
            <Dropdown menu={{ items: menuItems, onClick: ({ key }) => handleMenuAction(key) }}>
              <Button type="link" size="small" icon={<MoreOutlined />}>更多</Button>
            </Dropdown>
          </Space>
        );
      }
    }
  ], [openEditModal, handleEnable, handleDisable, handleDelete]);

  return (
    <div>
      <Typography.Title level={3}>规则管理</Typography.Title>

      {/* 搜索区域 */}
      <div className="simple-search-panel">
        <Space wrap>
          <Input
            placeholder="关键字（名称/描述）"
            value={keyword}
            onChange={e => setKeyword(e.target.value)}
            style={{ width: 200, height: 36 }}
            allowClear
          />
          <Select
            placeholder="所属智能体"
            value={filterAgentId}
            onChange={setFilterAgentId}
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
            options={[{ label: "启用", value: "ON" }, { label: "停用", value: "OFF" }]}
          />
          <Button type="primary" onClick={handleSearch}>搜索</Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
      </div>

      {/* 操作按钮区 */}
      <div className="simple-toolbar">
        <Space>
          <Button danger loading={batchDeleteLoading} onClick={handleBatchDelete} disabled={selectedRowKeys.length === 0}>批量删除</Button>
        </Space>
        <Button type="primary" onClick={openCreateModal}>新增规则</Button>
      </div>

      {/* 数据表格 */}
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

      {/* 创建/编辑弹窗 */}
      <Modal
        open={showModal}
        onCancel={() => setShowModal(false)}
        title={editingId ? "编辑规则" : "新增规则"}
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
              label="所属智能体"
              name="agentId"
              rules={[{ required: true, message: "请选择智能体" }]}
              style={{ marginBottom: 16 }}
            >
              <Select
                placeholder="选择智能体"
                style={{ height: 36 }}
                options={agents.map(a => ({ label: a.name, value: a.id }))}
                showSearch
                filterOption={(input, option) => (option?.label as string || "").includes(input)}
              />
            </Form.Item>
            <Form.Item
              label="定义描述"
              name="definitionDesc"
              rules={[{ required: true, message: "请输入定义描述" }]}
              style={{ marginBottom: 16 }}
            >
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item
              label="触发条件"
              name="triggerCondition"
              style={{ marginBottom: 16 }}
            >
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item
              label="触发动作"
              name="triggerAction"
              style={{ marginBottom: 16 }}
            >
              <Input.TextArea rows={3} />
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
