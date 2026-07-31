import type {MenuProps} from "antd";
import {Button, Dropdown, Form, Input, Modal, Popconfirm, Select, Space, Spin, Table, Tag, Tooltip, Typography} from "antd";
import type {ColumnsType} from "antd/es/table";
import {MoreOutlined} from "@ant-design/icons";
import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {usePreventDoubleClickHook} from "../hooks/usePreventDoubleClickHook";
import {useScrollSelect} from "../hooks/useScrollSelect";
import {ToastUtil} from "../utils/ToastUtil";
import {AgentMemoryApi} from "../api/agentMemoryApi";
import {AgentDefinitionApi} from "../api/agentDefinitionApi";
import type {
    AgentMemoryInfoResponseDto,
    AgentMemoryPageResponseDto,
    AgentMemoryStepDto,
    CreateAgentMemoryRequestDto,
    ParamsDefinitionResponseDto,
    UpdateAgentMemoryRequestDto
} from "../dto/agentMemory/AgentMemoryDto";

/**
 * 获取状态中文标签。
 *
 * @param status 状态
 * @returns 状态中文
 */
function getStatusLabel(status: string): string {
    if (status === "ON") return "启用";
    if (status === "OFF") return "停用";
    return status || "-";
}

/**
 * 获取状态Tag颜色。
 *
 * @param status 状态
 * @returns Tag颜色
 */
function getStatusColor(status: string): string {
    if (status === "ON") return "green";
    if (status === "OFF") return "default";
    return "default";
}

/**
 * 获取创建原因中文标签。
 *
 * @param reason 创建原因
 * @returns 中文标签
 */
function getCreateReasonLabel(reason: string): string {
    if (reason === "MANUAL") return "手动创建";
    if (reason === "AI_EXPLORATION") return "AI探索";
    if (reason === "MEMORY_REVISE") return "失败修订";
    return reason || "-";
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

    // 搜索条件
    const [keyword, setKeyword] = useState("");
    const [filterAgentId, setFilterAgentId] = useState<string | undefined>(undefined);
    const [pageIndex, setPageIndex] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 新建/编辑弹窗
    const [showModal, setShowModal] = useState(false);
    const [editingId, setEditingId] = useState<string | null>(null);
    const [form] = Form.useForm();

    // 执行弹窗
    const [showExecModal, setShowExecModal] = useState(false);
    const [execMemoryId, setExecMemoryId] = useState<string>("");
    const [execMemoryName, setExecMemoryName] = useState<string>("");
    const [execParamsDef, setExecParamsDef] = useState<Record<string, { type: string; description: string; required?: boolean }>>({});
    const [execSteps, setExecSteps] = useState<AgentMemoryStepDto[]>([]);
    const [execForm] = Form.useForm();

    // 详情弹窗
    const [showDetailModal, setShowDetailModal] = useState(false);
    const [detailData, setDetailData] = useState<AgentMemoryInfoResponseDto | null>(null);

    // 智能体下拉：滚动分页加载
    const {
        options: agentsRaw,
        loading: agentsLoading,
        onPopupScroll: onAgentsPopupScroll
    } = useScrollSelect<{ id: string; name?: string }>(
        (page, size) => AgentDefinitionApi.page({current: page, size}),
        20,
        []
    );

    const agents = useMemo(
        () => agentsRaw.filter(a => a.name).map(a => ({id: a.id, name: a.name!})),
        [agentsRaw]
    );

    // 加载列表数据
    const loadDataRef = useRef<(() => Promise<void>) | null>(null);
    const loadData = useCallback(async () => {
        setLoading(true);
        try {
            const result = await AgentMemoryApi.page({
                current: pageIndex,
                size: pageSize,
                memoryName: keyword || undefined,
                agentId: filterAgentId
            });
            setDataSource(result.records || []);
            setTotal(result.total || 0);
        } finally {
            setLoading(false);
        }
    }, [pageIndex, pageSize, keyword, filterAgentId]);

    loadDataRef.current = loadData;

    useEffect(() => {
        loadData();
    }, [loadData]);

    // 搜索
    const handleSearch = useCallback(() => {
        setPageIndex(1);
        loadDataRef.current?.();
    }, []);

    // 重置搜索
    const handleReset = useCallback(() => {
        setKeyword("");
        setFilterAgentId(undefined);
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
            const record = await AgentMemoryApi.findOne(id);
            form.setFieldsValue({
                agentId: record.agentId,
                memoryName: record.memoryName,
                paramsDefinition: record.paramsDefinition,
                summary: record.summary,
                remark: record.remark
            });
        } catch {
            ToastUtil.error("获取详情失败");
        }
        setShowModal(true);
    }, [form]);

    // 提交创建/编辑
    const {onClick: handleSubmit, loading: submitLoading} = usePreventDoubleClickHook(async () => {
        const values = await form.validateFields();
        if (editingId) {
            const updateData: UpdateAgentMemoryRequestDto = {id: editingId, ...values};
            await AgentMemoryApi.update(editingId, updateData);
            ToastUtil.success("更新成功");
        } else {
            const createData: CreateAgentMemoryRequestDto = values;
            await AgentMemoryApi.create(createData);
            ToastUtil.success("创建成功");
        }
        setShowModal(false);
        loadDataRef.current?.();
    });

    // 批量删除
    const {onClick: handleBatchDelete, loading: batchDeleteLoading} = usePreventDoubleClickHook(async () => {
        if (selectedRowKeys.length === 0) {
            ToastUtil.error("请先选择要删除的记录");
            return;
        }
        await AgentMemoryApi.deleteByIds(selectedRowKeys);
        ToastUtil.success("删除成功");
        setSelectedRowKeys([]);
        loadDataRef.current?.();
    });

    // 删除单个
    const handleDelete = useCallback(async (id: string) => {
        await AgentMemoryApi.deleteByIds([id]);
        ToastUtil.success("删除成功");
        loadDataRef.current?.();
    }, []);

    // 打开执行弹窗
    const openExecModal = useCallback(async (id: string) => {
        try {
            const result: ParamsDefinitionResponseDto = await AgentMemoryApi.getParamsDefinition(id);
            setExecMemoryId(id);
            setExecMemoryName(result.memoryName);
            setExecSteps(result.steps || []);

            // 解析参数定义
            let parsedDef: Record<string, { type: string; description: string; required?: boolean }> = {};
            if (result.paramsDefinition) {
                try {
                    parsedDef = JSON.parse(result.paramsDefinition);
                } catch {
                    // 解析失败使用空对象
                }
            }
            setExecParamsDef(parsedDef);
            execForm.resetFields();
            setShowExecModal(true);
        } catch {
            ToastUtil.error("获取参数定义失败");
        }
    }, [execForm]);

    // 提交执行
    const {onClick: handleExecSubmit, loading: execLoading} = usePreventDoubleClickHook(async () => {
        const values = await execForm.validateFields();
        const params: Record<string, unknown> = {};
        const clientId = values.clientId || undefined;

        // 从表单值中提取参数（排除clientId）
        Object.keys(values).forEach(key => {
            if (key !== "clientId") {
                params[key] = values[key];
            }
        });

        await AgentMemoryApi.execute(execMemoryId, {params, clientId});
        ToastUtil.success("记忆执行已提交");
        setShowExecModal(false);
        loadDataRef.current?.();
    });

    // 查看详情
    const handleViewDetail = useCallback(async (id: string) => {
        try {
            const record = await AgentMemoryApi.findOne(id);
            setDetailData(record);
            setShowDetailModal(true);
        } catch {
            ToastUtil.error("获取详情失败");
        }
    }, []);

    // 表格列定义
    const columns = useMemo<ColumnsType<AgentMemoryPageResponseDto>>(() => [
        {title: "智能体", dataIndex: "agentName", width: 120, ellipsis: true, render: v => <Tooltip title={v}>{v || "-"}</Tooltip>},
        {title: "记忆名称", dataIndex: "memoryName", width: 180, ellipsis: true, render: v => <Tooltip title={v}>{v}</Tooltip>},
        {
            title: "状态",
            dataIndex: "status",
            width: 90,
            render: (s: string) => <Tag color={getStatusColor(s)}>{getStatusLabel(s)}</Tag>
        },
        {title: "摘要", dataIndex: "summary", width: 200, ellipsis: true, render: v => <Tooltip title={v}>{v || "-"}</Tooltip>},
        {title: "创建原因", dataIndex: "createReason", width: 90, render: (v: string) => getCreateReasonLabel(v)},
        {title: "步骤数", dataIndex: "stepCount", width: 70},
        {title: "参数数", dataIndex: "paramCount", width: 70},
        {
            title: "备注", dataIndex: "remark", width: 120, ellipsis: true, render: v => {
                if (!v) return "-";
                const display = v.length > 15 ? v.substring(0, 15) + "..." : v;
                return <Tooltip title={v}>{display}</Tooltip>;
            }
        },
        {title: "修改时间", dataIndex: "updateTime", width: 160},
        {
            title: "操作", width: 160,
            render: (_: unknown, record: AgentMemoryPageResponseDto) => {
                const isEnabled = record.status === "ON";
                const menuItems: MenuProps["items"] = [
                    {key: "detail", label: "查看详情"},
                    isEnabled ? {key: "execute", label: "执行"} : null,
                    {
                        key: "delete",
                        label: <Popconfirm title="确认删除该记忆？" okButtonProps={{danger: true}} onConfirm={() => handleDelete(record.id)}>删除</Popconfirm>,
                        danger: true
                    }
                ].filter(Boolean) as MenuProps["items"];

                const handleMenuAction = (key: string) => {
                    if (key === "detail") {
                        void handleViewDetail(record.id);
                    }
                    if (key === "execute") {
                        void openExecModal(record.id);
                    }
                };

                return (
                    <Space>
                        <Button type="link" size="small" onClick={() => openEditModal(record.id)}>编辑</Button>
                        <Dropdown menu={{items: menuItems, onClick: ({key}) => handleMenuAction(key)}}>
                            <Button type="link" size="small" icon={<MoreOutlined/>}>更多</Button>
                        </Dropdown>
                    </Space>
                );
            }
        }
    ], [openEditModal, openExecModal, handleDelete, handleViewDetail]);

    return (
        <div>
            <Typography.Title level={3}>记忆编排管理</Typography.Title>

            {/* 搜索区域 */}
            <div className="simple-search-panel">
                <Space wrap>
                    <Input
                        placeholder="记忆名称"
                        value={keyword}
                        onChange={e => setKeyword(e.target.value)}
                        style={{width: 200, height: 36}}
                        allowClear
                    />
                    <Select
                        placeholder="所属智能体"
                        value={filterAgentId}
                        onChange={setFilterAgentId}
                        style={{width: 160, height: 36}}
                        allowClear
                        options={agents.map(a => ({label: a.name, value: a.id}))}
                        onPopupScroll={onAgentsPopupScroll}
                        notFoundContent={agentsLoading ? <Spin size="small"/> : "暂无数据"}
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
                <Button type="primary" onClick={openCreateModal}>新增记忆</Button>
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

            {/* 创建/编辑弹窗 */}
            <Modal
                open={showModal}
                onCancel={() => setShowModal(false)}
                title={editingId ? "编辑记忆" : "新增记忆"}
                width={560}
                centered
                footer={[
                    <Button key="cancel" onClick={() => setShowModal(false)}>取消</Button>,
                    <Button key="confirm" type="primary" loading={submitLoading} onClick={handleSubmit}>
                        {editingId ? "确认更新" : "确认创建"}
                    </Button>
                ]}
            >
                <div className="simple-form-container">
                    <Form form={form} layout="horizontal" size="middle" labelCol={{span: 5}} wrapperCol={{span: 19}}>
                        <Form.Item
                            label="所属智能体"
                            name="agentId"
                            rules={[{required: true, message: "请选择智能体"}]}
                            style={{marginBottom: 16}}
                        >
                            <Select
                                placeholder="选择智能体"
                                style={{height: 36}}
                                options={agents.map(a => ({label: a.name, value: a.id}))}
                                showSearch
                                onPopupScroll={onAgentsPopupScroll}
                                notFoundContent={agentsLoading ? <Spin size="small"/> : "暂无数据"}
                                filterOption={(input, option) => (option?.label as string || "").includes(input)}
                            />
                        </Form.Item>
                        <Form.Item
                            label="记忆名称"
                            name="memoryName"
                            rules={[{required: true, message: "请输入记忆名称"}]}
                            style={{marginBottom: 16}}
                        >
                            <Input placeholder="支持{param}占位符" style={{height: 36}}/>
                        </Form.Item>
                        <Form.Item
                            label="参数定义"
                            name="paramsDefinition"
                            style={{marginBottom: 16}}
                        >
                            <Input.TextArea rows={4} placeholder='{"paramName": {"type": "string", "description": "说明", "required": true}}'/>
                        </Form.Item>
                        <Form.Item
                            label="摘要"
                            name="summary"
                            style={{marginBottom: 16}}
                        >
                            <Input.TextArea rows={2}/>
                        </Form.Item>
                        <Form.Item label="备注" name="remark" style={{marginBottom: 0}}>
                            <Input.TextArea rows={2}/>
                        </Form.Item>
                    </Form>
                </div>
            </Modal>

            {/* 执行弹窗 */}
            <Modal
                open={showExecModal}
                onCancel={() => setShowExecModal(false)}
                title={`执行记忆：${execMemoryName}`}
                width={520}
                centered
                footer={[
                    <Button key="cancel" onClick={() => setShowExecModal(false)}>取消</Button>,
                    <Button key="exec" type="primary" loading={execLoading} onClick={handleExecSubmit}>执行</Button>
                ]}
            >
                <div className="simple-form-container">
                    <Form form={execForm} layout="horizontal" size="middle" labelCol={{span: 5}} wrapperCol={{span: 19}}>
                        {/* 动态参数表单 */}
                        {Object.entries(execParamsDef).map(([paramName, def]) => (
                            <Form.Item
                                key={paramName}
                                label={def.description || paramName}
                                name={paramName}
                                rules={def.required ? [{required: true, message: `请输入${def.description || paramName}`}] : undefined}
                                style={{marginBottom: 16}}
                            >
                                <Input placeholder={`${paramName}${def.required ? " (必填)" : ""}`}/>
                            </Form.Item>
                        ))}

                        {/* 步骤预览 */}
                        {execSteps.length > 0 && (
                            <Form.Item label="执行步骤" style={{marginBottom: 16}}>
                                <div style={{fontSize: 12, color: "#666"}}>
                                    {execSteps.map((step, idx) => (
                                        <div key={step.id || idx} style={{marginBottom: 4}}>
                                            {idx + 1}. {step.stepName}
                                            <Tag color="blue" style={{marginLeft: 4, fontSize: 10}}>{step.atomicCommandCode || "-"}</Tag>
                                        </div>
                                    ))}
                                </div>
                            </Form.Item>
                        )}

                        <Form.Item label="客户端ID" name="clientId" style={{marginBottom: 0}}>
                            <Input placeholder="不传则使用记忆绑定的客户端"/>
                        </Form.Item>
                    </Form>
                </div>
            </Modal>

            {/* 详情弹窗 */}
            <Modal
                open={showDetailModal}
                onCancel={() => setShowDetailModal(false)}
                title="记忆详情"
                width={600}
                centered
                footer={<Button onClick={() => setShowDetailModal(false)}>关闭</Button>}
            >
                {detailData && (
                    <div style={{lineHeight: 2}}>
                        <div><b>记忆名称：</b>{detailData.memoryName}</div>
                        <div><b>状态：</b><Tag
                            color={getStatusColor(detailData.status)}>{getStatusLabel(detailData.status)}</Tag></div>
                        <div><b>创建原因：</b>{getCreateReasonLabel(detailData.createReason)}</div>
                        <div><b>摘要：</b>{detailData.summary || "-"}</div>
                        <div><b>步骤数：</b>{detailData.steps?.length || 0}</div>
                        <div><b>备注：</b>{detailData.remark || "-"}</div>
                        <div><b>修改时间：</b>{detailData.updateTime}</div>
                    </div>
                )}
            </Modal>
        </div>
    );
}