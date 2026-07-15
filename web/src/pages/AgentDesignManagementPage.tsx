import { Button, Form, Input, Modal, Select, Space, Table, Tooltip, Typography, Popconfirm, Tag } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { usePreventDoubleClickHook } from "../hooks/usePreventDoubleClickHook";
import { ToastUtil } from "../utils/ToastUtil";
import { AgentDefinitionApi } from "../api/agentDefinitionApi";
import { AgentRuleApi } from "../api/agentRuleApi";
import { AgentSkillApi } from "../api/agentSkillApi";
import { AiModelApi } from "../api/aiModelApi";
import type {
  AgentDefinitionPageDto,
  AgentDefinitionInfoDto,
  CreateAgentDefinitionDto,
  UpdateAgentDefinitionDto
} from "../dto/agentDefinition/AgentDefinitionDto";
import type { AiModelResponseDto } from "../dto/aiModel/AiModelDto";
import type { AgentRulePageResponseDto } from "../dto/agentRule/AgentRuleDto";
import type { AgentSkillPageResponseDto } from "../dto/agentSkill/AgentSkillPageResponseDto";

/**
 * 获取状态中文标签。
 *
 * @param status 状态码
 * @returns 状态中文
 */
function getStatusLabel(status: number): string {
  if (status === 1) return "启用";
  if (status === 0) return "停用";
  return String(status);
}

/**
 * 获取执行状态颜色。
 *
 * @param status 执行状态
 * @returns 颜色值
 */
function getExecStatusColor(status?: string): string {
  if (status === "SUCCESS") return "green";
  if (status === "FAILED") return "red";
  if (status === "RUNNING") return "blue";
  return "default";
}

/**
 * 智能体设计管理页面。
 *
 * @author qty
 */
export function AgentDesignManagementPage() {
  const [dataSource, setDataSource] = useState<AgentDefinitionPageDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

  // 搜索条件
  const [keyword, setKeyword] = useState("");
  const [filterStatus, setFilterStatus] = useState<number | undefined>(undefined);
  const [pageIndex, setPageIndex] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  // 弹窗状态
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form] = Form.useForm();

  // 模型下拉数据
  const [models, setModels] = useState<AiModelResponseDto[]>([]);

  // 行内规则/技能管理弹窗状态
  const [ruleModalAgentId, setRuleModalAgentId] = useState<string | null>(null);
  const [skillModalAgentId, setSkillModalAgentId] = useState<string | null>(null);
  const [rulesDropdown, setRulesDropdown] = useState<{ id: string; label: string }[]>([]);
  const [skillsDropdown, setSkillsDropdown] = useState<{ id: string; label: string }[]>([]);
  const [showRuleCreate, setShowRuleCreate] = useState(false);
  const [showSkillCreate, setShowSkillCreate] = useState(false);
  const [ruleCreateForm] = Form.useForm();
  const [skillCreateForm] = Form.useForm();

  // 加载模型下拉
  const loadModels = useCallback(async () => {
    try {
      const result = await AiModelApi.list();
      setModels(result || []);
    } catch {
      // 下拉加载失败不影响主流程
    }
  }, []);

  // 加载列表数据
  const loadDataRef = useRef<(() => Promise<void>) | null>(null);
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await AgentDefinitionApi.page({
        current: pageIndex,
        size: pageSize,
        keyword: keyword || undefined,
        status: filterStatus
      });
      setDataSource(result.records || []);
      setTotal(result.total || 0);
    } finally {
      setLoading(false);
    }
  }, [pageIndex, pageSize, keyword, filterStatus]);

  loadDataRef.current = loadData;

  useEffect(() => {
    loadData();
    loadModels();
  }, [loadData, loadModels]);

  // 搜索
  const handleSearch = useCallback(() => {
    setPageIndex(1);
    loadDataRef.current?.();
  }, []);

  // 重置搜索
  const handleReset = useCallback(() => {
    setKeyword("");
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
      const record: AgentDefinitionInfoDto = await AgentDefinitionApi.findOne(id);
      form.setFieldsValue({
        name: record.name,
        definitionDesc: record.definitionDesc,
        defaultModelId: record.defaultModelId || undefined,
        remark: record.remark || ""
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
      const updateData: UpdateAgentDefinitionDto = { id: editingId, ...values };
      await AgentDefinitionApi.update(editingId, updateData);
      ToastUtil.success("更新成功");
    } else {
      const createData: CreateAgentDefinitionDto = values;
      await AgentDefinitionApi.create(createData);
      ToastUtil.success("创建成功");
    }
    setShowModal(false);
    loadDataRef.current?.();
  });

  // 批量删除（级联）
  const { onClick: handleBatchDelete, loading: batchDeleteLoading } = usePreventDoubleClickHook(async () => {
    if (selectedRowKeys.length === 0) {
      ToastUtil.error("请先选择要删除的记录");
      return;
    }
    await AgentDefinitionApi.cascadeDelete(selectedRowKeys);
    ToastUtil.success("删除成功");
    setSelectedRowKeys([]);
    loadDataRef.current?.();
  });

  // 删除单个（级联）
  const handleDelete = useCallback(async (id: string) => {
    await AgentDefinitionApi.cascadeDelete([id]);
    ToastUtil.success("删除成功");
    loadDataRef.current?.();
  }, []);

  // 加载该智能体的规则/技能下拉
  const openRuleModal = useCallback(async (agentId: string) => {
    setRuleModalAgentId(agentId);
    try {
      const result = await AgentRuleApi.listAll();
      setRulesDropdown((result?.records || []).map((r: AgentRulePageResponseDto) => ({ id: r.id, label: r.definitionDesc || r.id })));
    } catch { /* ignore */ }
  }, []);

  const openSkillModal = useCallback(async (agentId: string) => {
    setSkillModalAgentId(agentId);
    try {
      const result = await AgentSkillApi.listAll();
      setSkillsDropdown((result?.records || []).map((s: AgentSkillPageResponseDto) => ({ id: s.id, label: s.definitionDesc || s.id })));
    } catch { /* ignore */ }
  }, []);

  // mini 弹窗：创建规则（带 agentId）
  const { onClick: handleRuleCreate, loading: ruleCreateLoading } = usePreventDoubleClickHook(async () => {
    const values = await ruleCreateForm.validateFields();
    await AgentRuleApi.create({ ...values, agentId: ruleModalAgentId! });
    ToastUtil.success("规则已创建");
    setShowRuleCreate(false);
    if (ruleModalAgentId) openRuleModal(ruleModalAgentId);
  });

  // mini 弹窗：创建技能（带 agentId）
  const { onClick: handleSkillCreate, loading: skillCreateLoading } = usePreventDoubleClickHook(async () => {
    const values = await skillCreateForm.validateFields();
    await AgentSkillApi.create({ ...values, agentId: skillModalAgentId! });
    ToastUtil.success("技能已创建");
    setShowSkillCreate(false);
    if (skillModalAgentId) openSkillModal(skillModalAgentId);
  });

  // 模型名称映射
  const getModelLabel = useCallback((modelId?: string) => {
    if (!modelId) return "-";
    const m = models.find(mod => mod.id === modelId);
    return m ? `${m.providerName} · ${m.modelCode}` : modelId;
  }, [models]);

  // 表格列定义
  const columns = useMemo<ColumnsType<AgentDefinitionPageDto>>(() => [
    { title: "智能体名称", dataIndex: "name", width: 140 },
    { title: "定义描述", dataIndex: "definitionDesc", width: 180, ellipsis: true, render: (v: string) => <Tooltip title={v}>{v}</Tooltip> },
    {
      title: "默认模型", dataIndex: "defaultModelId", width: 160,
      render: (val: string) => {
        const label = getModelLabel(val);
        return <Tooltip title={label}>{label}</Tooltip>;
      }
    },
    { title: "技能数", dataIndex: "skillCount", width: 70 },
    { title: "规则数", dataIndex: "ruleCount", width: 70 },
    { title: "记忆数", dataIndex: "memoryCount", width: 70 },
    { title: "子智能体", dataIndex: "subAgentCount", width: 80 },
    {
      title: "最近任务", dataIndex: "recentTaskStatusLabel", width: 100,
      render: (v: string, r: AgentDefinitionPageDto) => <Tag color={getExecStatusColor(r.recentTaskStatus)}>{v || "-"}</Tag>
    },
    {
      title: "状态", dataIndex: "status", width: 70,
      render: (status: number) => <Tag color={status === 1 ? "green" : "red"}>{getStatusLabel(status)}</Tag>
    },
    { title: "修改时间", dataIndex: "updateTime", width: 160 },
    { title: "备注", dataIndex: "remark", ellipsis: true, render: (v: string) => <Tooltip title={v}>{v || "-"}</Tooltip> },
    {
      title: "操作", width: 260,
      render: (_: unknown, record: AgentDefinitionPageDto) => (
        <Space wrap size={[0, 4]}>
          <Button type="link" onClick={() => openEditModal(record.id)}>编辑</Button>
          <Button type="link" onClick={() => openRuleModal(record.id)}>规则</Button>
          <Button type="link" onClick={() => openSkillModal(record.id)}>技能</Button>
          <Popconfirm title="确定级联删除该智能体吗？将同时删除关联的技能/规则/记忆/任务等数据。" onConfirm={() => handleDelete(record.id)} okButtonProps={{ danger: true }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ], [getModelLabel, openEditModal, openRuleModal, openSkillModal, handleDelete]);

  return (
    <div>
      <Typography.Title level={3}>智能体设计管理</Typography.Title>

      {/* 搜索区域 */}
      <div className="simple-search-panel">
        <Space wrap>
          <Input
            placeholder="智能体名称/描述"
            value={keyword}
            onChange={e => setKeyword(e.target.value)}
            style={{ width: 200, height: 36 }}
            allowClear
          />
          <Select
            placeholder="状态"
            value={filterStatus}
            onChange={setFilterStatus}
            style={{ width: 120, height: 36 }}
            allowClear
            options={[{ label: "启用", value: 1 }, { label: "停用", value: 0 }]}
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
        <Button type="primary" onClick={openCreateModal}>新增智能体</Button>
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
        title={editingId ? "编辑智能体" : "新增智能体"}
        width={600}
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
              label="名称"
              name="name"
              rules={[{ required: true, message: "请输入名称" }]}
              style={{ marginBottom: 16 }}
            >
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item
              label="默认模型"
              name="defaultModelId"
              rules={[{ required: true, message: "请选择默认模型" }]}
              style={{ marginBottom: 16 }}
            >
              <Select
                placeholder="选择默认模型"
                style={{ height: 36 }}
                options={models.filter(m => m.status === 1).map(m => ({
                  label: `${m.providerName} · ${m.modelCode}`,
                  value: m.id
                }))}
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
            <Form.Item label="备注" name="remark" style={{ marginBottom: 0 }}>
              <Input.TextArea rows={3} />
            </Form.Item>
          </Form>
        </div>
      </Modal>

      {/* 规则管理弹窗 */}
      <Modal
        open={!!ruleModalAgentId}
        onCancel={() => setRuleModalAgentId(null)}
        title="管理规则"
        width={600}
        centered
        footer={null}
      >
        <div className="simple-form-container">
          <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
            <Select
              mode="multiple"
              placeholder="选择已有规则关联到此智能体"
              style={{ flex: 1 }}
              options={rulesDropdown}
              allowClear
              showSearch
              filterOption={(input, option) => (option?.label as string || "").includes(input)}
            />
            <Button icon={<PlusOutlined />} onClick={() => { ruleCreateForm.resetFields(); setShowRuleCreate(true); }}>
              新增
            </Button>
          </div>
          <Button type="primary" block onClick={() => setRuleModalAgentId(null)}>完成</Button>
        </div>
      </Modal>

      {/* 技能管理弹窗 */}
      <Modal
        open={!!skillModalAgentId}
        onCancel={() => setSkillModalAgentId(null)}
        title="管理技能"
        width={600}
        centered
        footer={null}
      >
        <div className="simple-form-container">
          <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
            <Select
              mode="multiple"
              placeholder="选择已有技能关联到此智能体"
              style={{ flex: 1 }}
              options={skillsDropdown}
              allowClear
              showSearch
              filterOption={(input, option) => (option?.label as string || "").includes(input)}
            />
            <Button icon={<PlusOutlined />} onClick={() => { skillCreateForm.resetFields(); setShowSkillCreate(true); }}>
              新增
            </Button>
          </div>
          <Button type="primary" block onClick={() => setSkillModalAgentId(null)}>完成</Button>
        </div>
      </Modal>

      {/* mini 弹窗：新增规则 */}
      <Modal
        open={showRuleCreate}
        onCancel={() => setShowRuleCreate(false)}
        title="新增规则"
        width={520}
        centered
        footer={[
          <Button key="cancel" onClick={() => setShowRuleCreate(false)}>取消</Button>,
          <Button key="confirm" type="primary" loading={ruleCreateLoading} onClick={handleRuleCreate}>确认创建</Button>
        ]}
      >
        <div className="simple-form-container">
          <Form form={ruleCreateForm} layout="horizontal" size="middle" labelCol={{ span: 5 }} wrapperCol={{ span: 19 }}>
            <Form.Item label="定义描述" name="definitionDesc" rules={[{ required: true, message: "请输入" }]} style={{ marginBottom: 16 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="触发条件" name="triggerCondition" rules={[{ required: true, message: "请输入" }]} style={{ marginBottom: 16 }}>
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item label="触发动作" name="triggerAction" rules={[{ required: true, message: "请输入" }]} style={{ marginBottom: 16 }}>
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item label="备注" name="remark" style={{ marginBottom: 0 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
          </Form>
        </div>
      </Modal>

      {/* mini 弹窗：新增技能 */}
      <Modal
        open={showSkillCreate}
        onCancel={() => setShowSkillCreate(false)}
        title="新增技能"
        width={520}
        centered
        footer={[
          <Button key="cancel" onClick={() => setShowSkillCreate(false)}>取消</Button>,
          <Button key="confirm" type="primary" loading={skillCreateLoading} onClick={handleSkillCreate}>确认创建</Button>
        ]}
      >
        <div className="simple-form-container">
          <Form form={skillCreateForm} layout="horizontal" size="middle" labelCol={{ span: 5 }} wrapperCol={{ span: 19 }}>
            <Form.Item label="定义描述" name="definitionDesc" rules={[{ required: true, message: "请输入" }]} style={{ marginBottom: 16 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="执行内容" name="execContent" rules={[{ required: true, message: "请输入" }]} style={{ marginBottom: 16 }}>
              <Input.TextArea rows={4} />
            </Form.Item>
            <Form.Item label="返回格式" name="returnDataFormat" rules={[{ required: true, message: "请输入" }]} style={{ marginBottom: 16 }}>
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item label="备注" name="remark" style={{ marginBottom: 0 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
          </Form>
        </div>
      </Modal>
    </div>
  );
}
