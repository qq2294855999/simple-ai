import type {MenuProps} from "antd";
import {Button, Dropdown, Input, Modal, Popconfirm, Select, Space, Spin, Table, Tag, Tooltip, Typography} from "antd";
import {MoreOutlined, PlusOutlined} from "@ant-design/icons";
import type {ColumnsType} from "antd/es/table";
import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {useNavigate} from "react-router-dom";
import {usePreventDoubleClickHook} from "../hooks/usePreventDoubleClickHook";
import {useScrollSelect} from "../hooks/useScrollSelect";
import {ToastUtil} from "../utils/ToastUtil";
import {AgentDefinitionApi} from "../api/agentDefinitionApi";
import {AgentRuleApi} from "../api/agentRuleApi";
import {AgentSkillApi} from "../api/agentSkillApi";
import {AiModelApi} from "../api/aiModelApi";
import type {AgentDefinitionPageDto} from "../dto/agentDefinition/AgentDefinitionDto";
import type {AiModelResponseDto} from "../dto/aiModel/AiModelDto";
import type {AgentRulePageResponseDto} from "../dto/agentRule/AgentRuleDto";
import type {AgentSkillPageResponseDto} from "../dto/agentSkill/AgentSkillPageResponseDto";

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
 * 新增/编辑/查看智能体均跳转到独立页面，不再使用弹窗。
 * 规则/技能关联管理仍保留弹窗，但编辑/新增操作跳转到独立页面。
 *
 * @author qty
 */
export function AgentDesignManagementPage() {
    const navigate = useNavigate();
    const [dataSource, setDataSource] = useState<AgentDefinitionPageDto[]>([]);
    const [loading, setLoading] = useState(false);
    const [total, setTotal] = useState(0);
    const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

    // 搜索条件
    const [keyword, setKeyword] = useState("");
    const [filterStatus, setFilterStatus] = useState<number | undefined>(undefined);
    const [pageIndex, setPageIndex] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 模型下拉数据
    const [models, setModels] = useState<AiModelResponseDto[]>([]);

    // 行内规则/技能管理弹窗状态
    const [ruleModalAgentId, setRuleModalAgentId] = useState<string | null>(null);
    const [skillModalAgentId, setSkillModalAgentId] = useState<string | null>(null);
    const [selectedRuleIds, setSelectedRuleIds] = useState<string[]>([]);
    const [selectedSkillIds, setSelectedSkillIds] = useState<string[]>([]);

    // 规则下拉：滚动分页加载（页面加载即开始，用于规则管理弹窗中的 Select）
    const {
        options: rulesDropdownRaw,
        loading: rulesDropdownLoading,
        onPopupScroll: onRulesDropdownPopupScroll
    } = useScrollSelect<AgentRulePageResponseDto>(
        (page, size) => AgentRuleApi.page({current: page, size}),
        20,
        []
    );

    const rulesDropdown = useMemo(
        () => rulesDropdownRaw.map(r => ({id: r.id, label: r.definitionDesc || r.id})),
        [rulesDropdownRaw]
    );

    // 技能下拉：滚动分页加载
    const {
        options: skillsDropdownRaw,
        loading: skillsDropdownLoading,
        onPopupScroll: onSkillsDropdownPopupScroll
    } = useScrollSelect<AgentSkillPageResponseDto>(
        (page, size) => AgentSkillApi.page({current: page, size}),
        20,
        []
    );

    const skillsDropdown = useMemo(
        () => skillsDropdownRaw.map(s => ({id: s.id, label: s.definitionDesc || s.id})),
        [skillsDropdownRaw]
    );

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

    // 切换启用/停用状态
    const handleToggleStatus = useCallback(async (id: string) => {
        try {
            const newStatus = await AgentDefinitionApi.toggleStatus(id);
            ToastUtil.success(`智能体已${newStatus === "ON" ? "启用" : "停用"}`);
            loadDataRef.current?.();
        } catch {
            ToastUtil.error("状态切换失败");
        }
    }, []);

    // 批量删除（级联）
    const {onClick: handleBatchDelete, loading: batchDeleteLoading} = usePreventDoubleClickHook(async () => {
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

    // 加载该智能体已关联的规则列表（下拉已由 useScrollSelect 自动加载）
    const loadRuleData = useCallback(async (agentId: string) => {
        try {
            const agentResult = await AgentRuleApi.page({current: 1, size: 200, agentId});
            setRuleItems(agentResult?.records || []);
        } catch {
            // 下拉加载失败不影响主流程
        }
    }, []);

    const openRuleModal = useCallback(async (agentId: string) => {
        setRuleModalAgentId(agentId);
        setSelectedRuleIds([]);
        await loadRuleData(agentId);
    }, [loadRuleData]);

    // 加载该智能体已关联的技能列表（下拉已由 useScrollSelect 自动加载）
    const loadSkillData = useCallback(async (agentId: string) => {
        try {
            const agentResult = await AgentSkillApi.page({current: 1, size: 200, agentId});
            setSkillItems(agentResult?.records || []);
        } catch {
            // 下拉加载失败不影响主流程
        }
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
    const {onClick: handleFinishRuleModal, loading: finishRuleLoading} = usePreventDoubleClickHook(async () => {
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
    const {onClick: handleFinishSkillModal, loading: finishSkillLoading} = usePreventDoubleClickHook(async () => {
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

    // 删除规则
    const handleRuleDelete = useCallback(async (ruleId: string) => {
        await AgentRuleApi.deleteByIds([ruleId]);
        ToastUtil.success("规则已删除");
        if (ruleModalAgentId) loadRuleData(ruleModalAgentId);
    }, [ruleModalAgentId, loadRuleData]);

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
        {title: "智能体名称", dataIndex: "name", width: 140},
        {
            title: "定义描述", dataIndex: "definitionDesc", width: 180, ellipsis: true,
            render: (v: string) => <Tooltip title={v}>{v}</Tooltip>
        },
        {
            title: "默认模型", dataIndex: "defaultModelId", width: 160,
            render: (val: string) => {
                const label = getModelLabel(val);
                return <Tooltip title={label}>{label}</Tooltip>;
            }
        },
        {title: "技能数", dataIndex: "skillCount", width: 70},
        {title: "规则数", dataIndex: "ruleCount", width: 70},
        {title: "记忆数", dataIndex: "memoryCount", width: 70},
        {title: "子智能体", dataIndex: "subAgentCount", width: 80},
        {
            title: "最近任务", dataIndex: "recentTaskStatusLabel", width: 100,
            render: (v: string, r: AgentDefinitionPageDto) => <Tag color={getExecStatusColor(r.recentTaskStatus)}>{v || "-"}</Tag>
        },
        {
            title: "状态", dataIndex: "status", width: 70,
            render: (status: number) => <Tag color={getStatusLabel(status) === "启用" ? "green" : "red"}>{getStatusLabel(status)}</Tag>
        },
        {title: "修改时间", dataIndex: "updateTime", width: 160},
        {
            title: "备注", dataIndex: "remark", ellipsis: true,
            render: (v: string) => <Tooltip title={v}>{v || "-"}</Tooltip>
        },
        {
            title: "操作", width: 180,
            render: (_: unknown, record: AgentDefinitionPageDto) => {
                const isEnabled = getStatusLabel(record.status ?? 0) === "启用";
                const menuItems: MenuProps["items"] = [
                    {key: "view", label: "查看"},
                    {key: "rule", label: "查看规则"},
                    {key: "skill", label: "查看技能"},
                    {key: "toggle", label: isEnabled ? "停用" : "启用"},
                    {
                        key: "delete",
                        label: (
                            <Popconfirm
                                title="确定级联删除该智能体及其关联数据吗？"
                                okButtonProps={{danger: true}}
                                onConfirm={() => handleDelete(record.id)}
                            >
                                级联删除
                            </Popconfirm>
                        ),
                        danger: true
                    }
                ];
                const handleMenuAction = (key: string) => {
                    if (key === "view") {
                        navigate(`/agent-design/${record.id}`);
                    }
                    if (key === "rule") {
                        openRuleModal(record.id);
                    }
                    if (key === "skill") {
                        openSkillModal(record.id);
                    }
                    if (key === "toggle") {
                        void handleToggleStatus(record.id);
                    }
                };
                return (
                    <Space>
                        <Button type="link" size="small"
                                onClick={() => navigate(`/agent-design/${record.id}/edit`)}>编辑</Button>
                        <Dropdown menu={{items: menuItems, onClick: ({key}) => handleMenuAction(key)}}>
                            <Button type="link" size="small" icon={<MoreOutlined/>}>更多</Button>
                        </Dropdown>
                    </Space>
                );
            }
        }
    ], [getModelLabel, navigate, openRuleModal, openSkillModal, handleToggleStatus, handleDelete]);

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
                        style={{width: 200, height: 36}}
                        allowClear
                    />
                    <Select
                        placeholder="状态"
                        value={filterStatus}
                        onChange={setFilterStatus}
                        style={{width: 120, height: 36}}
                        allowClear
                        options={[{label: "启用", value: 1}, {label: "停用", value: 2}]}
                    />
                    <Button type="primary" onClick={handleSearch}>搜索</Button>
                    <Button onClick={handleReset}>重置</Button>
                </Space>
            </div>

            {/* 操作按钮区 */}
            <div className="simple-toolbar">
                <Space>
                    <Button danger loading={batchDeleteLoading} onClick={handleBatchDelete}
                            disabled={selectedRowKeys.length === 0}>批量删除</Button>
                </Space>
                <Button type="primary" onClick={() => navigate("/agent-design/create")}>新增智能体</Button>
            </div>

            {/* 数据表格 */}
            <Table
                rowKey="id"
                bordered
                columns={columns}
                dataSource={dataSource}
                loading={loading}
                rowSelection={{selectedRowKeys, onChange: keys => setSelectedRowKeys(keys as string[])}}
                pagination={{
                    current: pageIndex,
                    pageSize,
                    total,
                    showSizeChanger: true,
                    showTotal: t => `共 ${t} 条`,
                    onChange: (p, s) => {
                        setPageIndex(p);
                        setPageSize(s);
                    }
                }}
            />

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
                    <Typography.Text strong style={{marginBottom: 8, display: "block"}}>已关联的规则</Typography.Text>
                    <Table<RuleTableRow>
                        rowKey="id"
                        bordered
                        size="small"
                        dataSource={ruleItems as RuleTableRow[]}
                        pagination={false}
                        scroll={{y: 200}}
                        style={{marginBottom: 16}}
                        columns={[
                            {
                                title: "定义描述", dataIndex: "definitionDesc", ellipsis: true,
                                render: (v: string) => <Tooltip title={v}>{v}</Tooltip>
                            },
                            {
                                title: "触发条件", dataIndex: "triggerCondition", width: 160, ellipsis: true,
                                render: (v: string) => <Tooltip title={v}>{v}</Tooltip>
                            },
                            {
                                title: "触发动作", dataIndex: "triggerAction", width: 160, ellipsis: true,
                                render: (v: string) => <Tooltip title={v}>{v}</Tooltip>
                            },
                            {
                                title: "状态", dataIndex: "status", width: 70,
                                render: (s: string) => <Tag color={getStatusLabel(s) === "启用" ? "green" : "red"}>{getStatusLabel(s)}</Tag>
                            },
                            {
                                title: "操作", width: 130,
                                render: (_: unknown, row: RuleTableRow) => (
                                    <Space size={[0, 4]}>
                                        <Button type="link" size="small"
                                                onClick={() => navigate(`/agent-rule/${row.id}/edit`)}>编辑</Button>
                                        <Popconfirm title="确定删除该规则吗？"
                                                    onConfirm={() => handleRuleDelete(row.id)}
                                                    okButtonProps={{danger: true}}>
                                            <Button type="link" size="small" danger>删除</Button>
                                        </Popconfirm>
                                    </Space>
                                )
                            }
                        ]}
                    />

                    {/* 关联新规则区域 */}
                    <div style={{display: "flex", gap: 8, marginBottom: 16}}>
                        <Select
                            mode="multiple"
                            placeholder="选择已有规则关联到此智能体"
                            style={{flex: 1}}
                            value={selectedRuleIds}
                            onChange={setSelectedRuleIds}
                            options={availableRulesDropdown}
                            fieldNames={{label: 'label', value: 'id'}}
                            allowClear
                            showSearch
                            onPopupScroll={onRulesDropdownPopupScroll}
                            notFoundContent={rulesDropdownLoading ? <Spin size="small"/> : "暂无数据"}
                            filterOption={(input, option) => (option?.label as string || "").includes(input)}
                        />
                        <Button icon={<PlusOutlined/>}
                                onClick={() => navigate("/agent-rule/create")}>
                            新增
                        </Button>
                    </div>
                    <Button type="primary" block loading={finishRuleLoading}
                            onClick={handleFinishRuleModal}>完成</Button>
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
                    <Typography.Text strong style={{marginBottom: 8, display: "block"}}>已关联的技能</Typography.Text>
                    <Table<SkillTableRow>
                        rowKey="id"
                        bordered
                        size="small"
                        dataSource={skillItems as SkillTableRow[]}
                        pagination={false}
                        scroll={{y: 200}}
                        style={{marginBottom: 16}}
                        columns={[
                            {
                                title: "定义描述", dataIndex: "definitionDesc", ellipsis: true,
                                render: (v: string) => <Tooltip title={v}>{v}</Tooltip>
                            },
                            {
                                title: "执行内容", dataIndex: "execContent", width: 160, ellipsis: true,
                                render: (v: string) => <Tooltip title={v}>{v}</Tooltip>
                            },
                            {
                                title: "返回格式", dataIndex: "returnDataFormat", width: 120, ellipsis: true,
                                render: (v: string) => <Tooltip title={v}>{v}</Tooltip>
                            },
                            {
                                title: "状态", dataIndex: "status", width: 70,
                                render: (s: string) => <Tag color={getStatusLabel(s) === "启用" ? "green" : "red"}>{getStatusLabel(s)}</Tag>
                            },
                            {
                                title: "操作", width: 130,
                                render: (_: unknown, row: SkillTableRow) => (
                                    <Space size={[0, 4]}>
                                        <Button type="link" size="small"
                                                onClick={() => navigate(`/agent-skill/${row.id}/edit`)}>编辑</Button>
                                        <Popconfirm title="确定删除该技能吗？"
                                                    onConfirm={() => handleSkillDelete(row.id)}
                                                    okButtonProps={{danger: true}}>
                                            <Button type="link" size="small" danger>删除</Button>
                                        </Popconfirm>
                                    </Space>
                                )
                            }
                        ]}
                    />

                    {/* 关联新技能区域 */}
                    <div style={{display: "flex", gap: 8, marginBottom: 16}}>
                        <Select
                            mode="multiple"
                            placeholder="选择已有技能关联到此智能体"
                            style={{flex: 1}}
                            value={selectedSkillIds}
                            onChange={setSelectedSkillIds}
                            options={availableSkillsDropdown}
                            fieldNames={{label: 'label', value: 'id'}}
                            allowClear
                            showSearch
                            onPopupScroll={onSkillsDropdownPopupScroll}
                            notFoundContent={skillsDropdownLoading ? <Spin size="small"/> : "暂无数据"}
                            filterOption={(input, option) => (option?.label as string || "").includes(input)}
                        />
                        <Button icon={<PlusOutlined/>}
                                onClick={() => navigate("/agent-skill/create")}>
                            新增
                        </Button>
                    </div>
                    <Button type="primary" block loading={finishSkillLoading}
                            onClick={handleFinishSkillModal}>完成</Button>
                </div>
            </Modal>
        </div>
    );
}