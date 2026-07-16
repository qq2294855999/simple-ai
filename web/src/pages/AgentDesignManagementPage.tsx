import { Button, Dropdown, Form, Input, Modal, Select, Space, Table, Tooltip, Typography, Popconfirm, Tag } from "antd";
import type { MenuProps } from "antd";
import { MoreOutlined, PlusOutlined } from "@ant-design/icons";
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
 * 规则表格列类型（用于规则管理弹窗内的表格）。
 */
interface RuleTableRow {
  id: string;
  definitionDesc: string;
  triggerCondition: string;
  triggerAction: string;
  status: string;
}

/**
 * 技能表格列类型（用于技能管理弹窗内的表格）。
 */
interface SkillTableRow {
  id: string;
  definitionDesc: string;
  execContent: string;
  returnDataFormat: string;
  status: string;
}

/**
 * 获取状态中文标签。
 *
 * @param status 状态码
 * @returns 状态中文
 */
function getStatusLabel(status: number | string): string {
  if (status === 1 || status === "ON") return "启用";
  if (status === 2 || status === "OFF") return "停用";
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
  const [selectedRuleIds, setSelectedRuleIds] = useState<string[]>([]);
  const [selectedSkillIds, setSelectedSkillIds] = useState<string[]>([]);
  const [showRuleCreate, setShowRuleCreate] = useState(false);
  const [showSkillCreate, setShowSkillCreate] = useState(false);
  const [ruleCreateForm] = Form.useForm();
  const [skillCreateForm] = Form.useForm();

  // 编辑规则/技能时的记录ID
  const [editingRuleId, setEditingRuleId] = useState<string | null>(null);
  const [editingSkillId, setEditingSkillId] = useState<string | null>(null);

  // 该智能体已关联的规则/技能列表
  const [ruleItems, setRuleItems] = useState<AgentRulePageResponseDto[]>([]);
  const [skillItems, setSkillItems] = useState<AgentSkillPageResponseDto[]>([]);

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

  // 启用
  const handleEnable = useCallback(async (id: string) => {
    await AgentDefinitionApi.enable(id);
    ToastUtil.success("已启用");
    loadDataRef.current?.();
  }, []);

  // 禁用
  const handleDisable = useCallback(async (id: string) => {
    await AgentDefinitionApi.disable(id);
    ToastUtil.success("已禁用");
    loadDataRef.current?.();
  }, []);

  // 加载该智能体的规则下拉和已关联规则列表
  const loadRuleData = useCallback(async (agentId: string) => {
    try {
      // 加载全量下拉选项
      const allResult = await AgentRuleApi.listAll();
      setRulesDropdown((allResult?.records || []).map((r: AgentRulePageResponseDto) => ({ id: r.id, label: r.definitionDesc || r.id })));

      // 加载该智能体已关联的规则
      const agentResult = await AgentRuleApi.page({ current: 1, size: 1000, agentId });
      setRuleItems(agentResult?.records || []);
    } catch { /* ignore */ }
  }, []);

  const openRuleModal = useCallback(async (agentId: string) => {
    setRuleModalAgentId(agentId);
    setSelectedRuleIds([]);
    await loadRuleData(agentId);
  }, [loadRuleData]);

  // 加载该智能体的技能下拉和已关联技能列表
  const loadSkillData = useCallback(async (agentId: string) => {
    try {
      // 加载全量下拉选项
      const allResult = await AgentSkillApi.listAll();
      setSkillsDropdown((allResult?.records || []).map((s: AgentSkillPageResponseDto) => ({ id: s.id, label: s.definitionDesc || s.id })));

      // 加载该智能体已关联的技能
      const agentResult = await AgentSkillApi.page({ current: 1, size: 1000, agentId });
      setSkillItems(agentResult?.records || []);
    } catch { /* ignore */ }
  }, []);

  const openSkillModal = useCallback(async (agentId: string) => {
    setSkillModalAgentId(agentId);
    setSelectedSkillIds([]);
    await loadSkillData(agentId);
  }, [loadSkillData]);

  // 过滤下拉选项，排除已关联的规则（验重）
  const availableRulesDropdown = useMemo(() => {
    const associatedIds = new Set(ruleItems.map(r => r.id));
    return rulesDropdown.filter(opt => !associatedIds.has(opt.id));
  }, [rulesDropdown, ruleItems]);

  // 过滤下拉选项，排除已关联的技能（验重）
  const availableSkillsDropdown = useMemo(() => {
    const associatedIds = new Set(skillItems.map(s => s.id));
    return skillsDropdown.filter(opt => !associatedIds.has(opt.id));
  }, [skillsDropdown, skillItems]);

  // 完成规则关联（验重：排除已关联的规则）
  const { onClick: handleFinishRuleModal, loading: finishRuleLoading } = usePreventDoubleClickHook(async () => {
    // 过滤掉已关联的规则ID
    const associatedIds = new Set(ruleItems.map(r => r.id));
    const newRuleIds = selectedRuleIds.filter(id => !associatedIds.has(id));

    if (newRuleIds.length === 0) {
      ToastUtil.error("所选规则已全部关联，无需重复操作");
      return;
    }

    for (const ruleId of newRuleIds) {

      // 查询规则当前数据
      const rule = await AgentRuleApi.findOne(ruleId);

      // 更新 agentId 为当前智能体
      await AgentRuleApi.update(ruleId, {
        id: ruleId,
        agentId: ruleModalAgentId!,
        definitionDesc: rule.definitionDesc,
        triggerCondition: rule.triggerCondition,
        triggerAction: rule.triggerAction,
        remark: rule.remark
      });
    }
    ToastUtil.success("已关联 " + newRuleIds.length + " 条规则");
    setRuleModalAgentId(null);
  });

  // 完成技能关联（验重：排除已关联的技能）
  const { onClick: handleFinishSkillModal, loading: finishSkillLoading } = usePreventDoubleClickHook(async () => {
    // 过滤掉已关联的技能ID
    const associatedIds = new Set(skillItems.map(s => s.id));
    const newSkillIds = selectedSkillIds.filter(id => !associatedIds.has(id));

    if (newSkillIds.length === 0) {
      ToastUtil.error("所选技能已全部关联，无需重复操作");
      return;
    }

    for (const skillId of newSkillIds) {

      // 查询技能当前数据
      const skill = await AgentSkillApi.findOne(skillId);

      // 更新 agentId 为当前智能体
      await AgentSkillApi.update(skillId, {
        id: skillId,
        agentId: skillModalAgentId!,
        definitionDesc: skill.definitionDesc,
        execContent: skill.execContent,
        returnDataFormat: skill.returnDataFormat,
        remark: skill.remark
      });
    }
    ToastUtil.success("已关联 " + newSkillIds.length + " 条技能");
    setSkillModalAgentId(null);
  });
  // mini 弹窗：提交规则（创建或编辑）
  const { onClick: handleRuleSubmit, loading: ruleSubmitLoading } = usePreventDoubleClickHook(async () => {
    const values = await ruleCreateForm.validateFields();
    if (editingRuleId) {
      // 编辑模式
      await AgentRuleApi.update(editingRuleId, { id: editingRuleId, ...values, agentId: ruleModalAgentId! });
      ToastUtil.success("规则已更新");
    } else {
      // 创建模式
      await AgentRuleApi.create({ ...values, agentId: ruleModalAgentId! });
      ToastUtil.success("规则已创建");
    }
    setShowRuleCreate(false);
    setEditingRuleId(null);
    if (ruleModalAgentId) loadRuleData(ruleModalAgentId);
  });

  // 打开编辑规则弹窗（含回显）
  const openRuleEditModal = useCallback(async (ruleId: string) => {
    setEditingRuleId(ruleId);
    try {
      const record = await AgentRuleApi.findOne(ruleId);
      ruleCreateForm.setFieldsValue({
        definitionDesc: record.definitionDesc,
        triggerCondition: record.triggerCondition,
        triggerAction: record.triggerAction,
        remark: record.remark || ""
      });
    } catch {
      ToastUtil.error("获取规则详情失败");
    }
    setShowRuleCreate(true);
  }, [ruleCreateForm]);

  // 删除规则
  const handleRuleDelete = useCallback(async (ruleId: string) => {
    await AgentRuleApi.deleteByIds([ruleId]);
    ToastUtil.success("规则已删除");
    if (ruleModalAgentId) loadRuleData(ruleModalAgentId);
  }, [ruleModalAgentId, loadRuleData]);

  // mini 弹窗：提交技能（创建或编辑）
  const { onClick: handleSkillSubmit, loading: skillSubmitLoading } = usePreventDoubleClickHook(async () => {
    const values = await skillCreateForm.validateFields();
    if (editingSkillId) {
      // 编辑模式
      await AgentSkillApi.update(editingSkillId, { id: editingSkillId, ...values, agentId: skillModalAgentId! });
      ToastUtil.success("技能已更新");
    } else {
      // 创建模式
      await AgentSkillApi.create({ ...values, agentId: skillModalAgentId! });
      ToastUtil.success("技能已创建");
    }
    setShowSkillCreate(false);
    setEditingSkillId(null);
    if (skillModalAgentId) loadSkillData(skillModalAgentId);
  });

  // 打开编辑技能弹窗（含回显）
  const openSkillEditModal = useCallback(async (skillId: string) => {
    setEditingSkillId(skillId);
    try {
      const record = await AgentSkillApi.findOne(skillId);
      skillCreateForm.setFieldsValue({
        definitionDesc: record.definitionDesc,
        execContent: record.execContent,
        returnDataFormat: record.returnDataFormat,
        remark: record.remark || ""
      });
    } catch {
      ToastUtil.error("获取技能详情失败");
    }
    setShowSkillCreate(true);
  }, [skillCreateForm]);

  // 删除技能
  const handleSkillDelete = useCallback(async (skillId: string) => {
    await AgentSkillApi.deleteByIds([skillId]);
    ToastUtil.success("技能已删除");
    if (skillModalAgentId) loadSkillData(skillModalAgentId);
  }, [skillModalAgentId, loadSkillData]);

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
      render: (status: number) => <Tag color={getStatusLabel(status) === "启用" ? "green" : "red"}>{getStatusLabel(status)}</Tag>
    },
    { title: "修改时间", dataIndex: "updateTime", width: 160 },
    { title: "备注", dataIndex: "remark", ellipsis: true, render: (v: string) => <Tooltip title={v}>{v || "-"}</Tooltip> },
    {
      title: "操作", width: 150,
      render: (_: unknown, record: AgentDefinitionPageDto) => {
        const isEnabled = getStatusLabel(record.status ?? 0) === "启用";
        const menuItems: MenuProps["items"] = [
          { key: "rule", label: "查看规则" },
          { key: "skill", label: "查看技能" },
          { key: "toggle", label: isEnabled ? "停用" : "启用" },
          { key: "delete", label: "级联删除", danger: true }
        ];
        const handleMenuAction = (key: string) => {
          if (key === "rule") { openRuleModal(record.id); }
          if (key === "skill") { openSkillModal(record.id); }
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
  ], [getModelLabel, openEditModal, openRuleModal, openSkillModal, handleEnable, handleDisable, handleDelete]);

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
            options={[{ label: "启用", value: 1 }, { label: "停用", value: 2 }]}
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
        width={800}
        centered
        footer={null}
      >
        <div className="simple-form-container">
          {/* 已关联规则表格 */}
          <Typography.Text strong style={{ marginBottom: 8, display: "block" }}>已关联的规则</Typography.Text>
          <Table<RuleTableRow>
            rowKey="id"
            bordered
            size="small"
            dataSource={ruleItems as RuleTableRow[]}
            pagination={false}
            scroll={{ y: 200 }}
            style={{ marginBottom: 16 }}
            columns={[
              { title: "定义描述", dataIndex: "definitionDesc", ellipsis: true, render: (v: string) => <Tooltip title={v}>{v}</Tooltip> },
              { title: "触发条件", dataIndex: "triggerCondition", width: 160, ellipsis: true, render: (v: string) => <Tooltip title={v}>{v}</Tooltip> },
              { title: "触发动作", dataIndex: "triggerAction", width: 160, ellipsis: true, render: (v: string) => <Tooltip title={v}>{v}</Tooltip> },
              {
                title: "状态", dataIndex: "status", width: 70,
                render: (s: string) => <Tag color={getStatusLabel(s) === "启用" ? "green" : "red"}>{getStatusLabel(s)}</Tag>
              },
              {
                title: "操作", width: 130,
                render: (_: unknown, row: RuleTableRow) => (
                  <Space size={[0, 4]}>
                    <Button type="link" size="small" onClick={() => openRuleEditModal(row.id)}>编辑</Button>
                    <Popconfirm title="确定删除该规则吗？" onConfirm={() => handleRuleDelete(row.id)} okButtonProps={{ danger: true }}>
                      <Button type="link" size="small" danger>删除</Button>
                    </Popconfirm>
                  </Space>
                )
              }
            ]}
          />

          {/* 关联新规则区域 */}
          <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
            <Select
              mode="multiple"
              placeholder="选择已有规则关联到此智能体"
              style={{ flex: 1 }}
              value={selectedRuleIds}
              onChange={setSelectedRuleIds}
              options={availableRulesDropdown}
              fieldNames={{ label: 'label', value: 'id' }}
              allowClear
              showSearch
              filterOption={(input, option) => (option?.label as string || "").includes(input)}
            />
            <Button icon={<PlusOutlined />} onClick={() => { setEditingRuleId(null); ruleCreateForm.resetFields(); setShowRuleCreate(true); }}>
              新增
            </Button>
          </div>
          <Button type="primary" block loading={finishRuleLoading} onClick={handleFinishRuleModal}>完成</Button>
        </div>
      </Modal>

      {/* 技能管理弹窗 */}
      <Modal
        open={!!skillModalAgentId}
        onCancel={() => setSkillModalAgentId(null)}
        title="管理技能"
        width={800}
        centered
        footer={null}
      >
        <div className="simple-form-container">
          {/* 已关联技能表格 */}
          <Typography.Text strong style={{ marginBottom: 8, display: "block" }}>已关联的技能</Typography.Text>
          <Table<SkillTableRow>
            rowKey="id"
            bordered
            size="small"
            dataSource={skillItems as SkillTableRow[]}
            pagination={false}
            scroll={{ y: 200 }}
            style={{ marginBottom: 16 }}
            columns={[
              { title: "定义描述", dataIndex: "definitionDesc", ellipsis: true, render: (v: string) => <Tooltip title={v}>{v}</Tooltip> },
              { title: "执行内容", dataIndex: "execContent", width: 160, ellipsis: true, render: (v: string) => <Tooltip title={v}>{v}</Tooltip> },
              { title: "返回格式", dataIndex: "returnDataFormat", width: 120, ellipsis: true, render: (v: string) => <Tooltip title={v}>{v}</Tooltip> },
              {
                title: "状态", dataIndex: "status", width: 70,
                render: (s: string) => <Tag color={getStatusLabel(s) === "启用" ? "green" : "red"}>{getStatusLabel(s)}</Tag>
              },
              {
                title: "操作", width: 130,
                render: (_: unknown, row: SkillTableRow) => (
                  <Space size={[0, 4]}>
                    <Button type="link" size="small" onClick={() => openSkillEditModal(row.id)}>编辑</Button>
                    <Popconfirm title="确定删除该技能吗？" onConfirm={() => handleSkillDelete(row.id)} okButtonProps={{ danger: true }}>
                      <Button type="link" size="small" danger>删除</Button>
                    </Popconfirm>
                  </Space>
                )
              }
            ]}
          />

          {/* 关联新技能区域 */}
          <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
            <Select
              mode="multiple"
              placeholder="选择已有技能关联到此智能体"
              style={{ flex: 1 }}
              value={selectedSkillIds}
              onChange={setSelectedSkillIds}
              options={availableSkillsDropdown}
              fieldNames={{ label: 'label', value: 'id' }}
              allowClear
              showSearch
              filterOption={(input, option) => (option?.label as string || "").includes(input)}
            />
            <Button icon={<PlusOutlined />} onClick={() => { setEditingSkillId(null); skillCreateForm.resetFields(); setShowSkillCreate(true); }}>
              新增
            </Button>
          </div>
          <Button type="primary" block loading={finishSkillLoading} onClick={handleFinishSkillModal}>完成</Button>
        </div>
      </Modal>

      {/* mini 弹窗：新增/编辑规则 */}
      <Modal
        open={showRuleCreate}
        onCancel={() => { setShowRuleCreate(false); setEditingRuleId(null); }}
        title={editingRuleId ? "编辑规则" : "新增规则"}
        width={600}
        centered
        footer={[
          <Button key="cancel" onClick={() => { setShowRuleCreate(false); setEditingRuleId(null); }}>取消</Button>,
          <Button key="confirm" type="primary" loading={ruleSubmitLoading} onClick={handleRuleSubmit}>
            {editingRuleId ? "确认更新" : "确认创建"}
          </Button>
        ]}
      >
        <div className="simple-form-container">
          <Form form={ruleCreateForm} layout="horizontal" size="middle" labelCol={{ span: 5 }} wrapperCol={{ span: 19 }}>
            <Form.Item label="定义描述" name="definitionDesc" rules={[{ required: true, message: "请输入" }]} style={{ marginBottom: 16 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="触发条件" name="triggerCondition" style={{ marginBottom: 16 }}>
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item label="触发动作" name="triggerAction" style={{ marginBottom: 16 }}>
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item label="备注" name="remark" style={{ marginBottom: 0 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
          </Form>
        </div>
      </Modal>

      {/* mini 弹窗：新增/编辑技能 */}
      <Modal
        open={showSkillCreate}
        onCancel={() => { setShowSkillCreate(false); setEditingSkillId(null); }}
        title={editingSkillId ? "编辑技能" : "新增技能"}
        width={600}
        centered
        footer={[
          <Button key="cancel" onClick={() => { setShowSkillCreate(false); setEditingSkillId(null); }}>取消</Button>,
          <Button key="confirm" type="primary" loading={skillSubmitLoading} onClick={handleSkillSubmit}>
            {editingSkillId ? "确认更新" : "确认创建"}
          </Button>
        ]}
      >
        <div className="simple-form-container">
          <Form form={skillCreateForm} layout="horizontal" size="middle" labelCol={{ span: 5 }} wrapperCol={{ span: 19 }}>
            <Form.Item label="定义描述" name="definitionDesc" rules={[{ required: true, message: "请输入" }]} style={{ marginBottom: 16 }}>
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item label="执行内容" name="execContent" rules={[{ required: true, message: "请输入" }]} style={{ marginBottom: 16 }}>
              <Input.TextArea rows={4} />
            </Form.Item>
            <Form.Item label="返回格式" name="returnDataFormat" style={{ marginBottom: 16 }}>
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
