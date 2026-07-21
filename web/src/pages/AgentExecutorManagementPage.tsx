import type {MenuProps} from "antd";
import {Button, Collapse, Descriptions, Dropdown, Form, Input, Modal, Select, Space, Table, Tag, Tooltip, Typography} from "antd";
import type {ColumnsType} from "antd/es/table";
import {BookOutlined, MoreOutlined} from "@ant-design/icons";
import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {useNavigate} from "react-router-dom";
import {usePreventDoubleClickHook} from "../hooks/usePreventDoubleClickHook";
import {ToastUtil} from "../utils/ToastUtil";
import {AgentExecutorApi} from "../api/agentExecutorApi";
import type {
    AgentExecutorPageResponseDto,
    AgentExecutorProtocolResponse,
    CreateAgentExecutorRequestDto,
    UpdateAgentExecutorRequestDto
} from "../dto/agentExecutor/AgentExecutorDto";

/**
 * 获取状态中文标签。
 *
 * @param status 状态码或状态名
 * @returns 状态中文
 */
function getStatusLabel(status: number | string): string {
    if (status === 1 || status === "ENABLE") return "启用";
    if (status === 2 || status === "DISABLE") return "停用";
    return String(status) || "-";
}

/**
 * 执行器类型管理页面。
 *
 * @author qty
 */
export function AgentExecutorManagementPage() {
    const navigate = useNavigate();
    const [dataSource, setDataSource] = useState<AgentExecutorPageResponseDto[]>([]);
    const [loading, setLoading] = useState(false);
    const [total, setTotal] = useState(0);
    const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

    // 搜索条件
    const [keyword, setKeyword] = useState("");
    const [filterStatus, setFilterStatus] = useState<string | undefined>(undefined);
    const [pageIndex, setPageIndex] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 弹窗状态
    const [showModal, setShowModal] = useState(false);
    const [editingId, setEditingId] = useState<string | null>(null);
    const [form] = Form.useForm();

    // 协议展示状态
    const [showProtocolModal, setShowProtocolModal] = useState(false);
    const [protocolData, setProtocolData] = useState<AgentExecutorProtocolResponse | null>(null);
    const [protocolLoading, setProtocolLoading] = useState(false);

    // 打开协议说明弹窗
    const handleOpenProtocol = useCallback(async () => {
        setProtocolLoading(true);
        try {
            const data = await AgentExecutorApi.getProtocol();
            setProtocolData(data);
            setShowProtocolModal(true);
        } catch {
            ToastUtil.error("获取协议说明失败");
        } finally {
            setProtocolLoading(false);
        }
    }, []);

    // 加载列表数据
    const loadDataRef = useRef<(() => Promise<void>) | null>(null);
    const loadData = useCallback(async () => {
        setLoading(true);
        try {
            const result = await AgentExecutorApi.page({
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
    }, [loadData]);

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

    // 切换启用/停用状态
    const handleToggleStatus = useCallback(async (id: string, currentStatus: string) => {
        try {
            const newStatus = await AgentExecutorApi.toggleStatus(id);
            ToastUtil.success(`执行器已${newStatus === "ENABLE" ? "启用" : "停用"}`);
            loadDataRef.current?.();
        } catch {
            ToastUtil.error("状态切换失败");
        }
    }, []);

    // 打开编辑弹窗
    const openEditModal = useCallback(async (id: string) => {
        setEditingId(id);
        try {
            const record = await AgentExecutorApi.findOne(id);
            form.setFieldsValue({
                executorCode: record.executorCode,
                executorName: record.executorName,
                description: record.description,
                status: record.status,
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
            const updateData: UpdateAgentExecutorRequestDto = {id: editingId, ...values};
            await AgentExecutorApi.update(editingId, updateData);
            ToastUtil.success("更新成功");
        } else {
            const createData: CreateAgentExecutorRequestDto = values;
            await AgentExecutorApi.create(createData);
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
        await AgentExecutorApi.deleteByIds(selectedRowKeys);
        ToastUtil.success("删除成功");
        setSelectedRowKeys([]);
        loadDataRef.current?.();
    });

    // 删除单个
    const handleDelete = useCallback(async (id: string) => {
        await AgentExecutorApi.deleteByIds([id]);
        ToastUtil.success("删除成功");
        loadDataRef.current?.();
    }, []);

    // 表格列定义
    const columns = useMemo<ColumnsType<AgentExecutorPageResponseDto>>(() => [
        {title: "执行器编码", dataIndex: "executorCode", width: 140},
        {title: "执行器名称", dataIndex: "executorName", width: 160},
        {title: "描述", dataIndex: "description", width: 200, ellipsis: true, render: value => <Tooltip title={value}>{value || "-"}</Tooltip>},
        {
            title: "状态",
            dataIndex: "status",
            width: 80,
            render: (status: string) => <Tag color={getStatusLabel(status) === "启用" ? "green" : "red"}>{getStatusLabel(status)}</Tag>
        },
        {title: "备注", dataIndex: "remark", ellipsis: true, render: value => <Tooltip title={value}>{value || "-"}</Tooltip>},
        {title: "创建时间", dataIndex: "createTime", width: 160},
        {title: "修改时间", dataIndex: "updateTime", width: 160},
        {
            title: "操作", width: 180,
            render: (_: unknown, record: AgentExecutorPageResponseDto) => {
                const isEnabled = getStatusLabel(record.status) === "启用";
                const menuItems: MenuProps["items"] = [
                    {key: "enable", label: isEnabled ? "停用" : "启用"},
                    {key: "addClient", label: "新增客户端"},
                    {key: "delete", label: "删除", danger: true}
                ];
                const handleMenuAction = (key: string) => {
                    if (key === "enable") {
                        void handleToggleStatus(record.id, record.status);
                    }
                    if (key === "addClient") {
                        navigate(`/agent-client?executorId=${record.id}&executorName=${encodeURIComponent(record.executorName)}`);
                    }
                    if (key === "delete") {
                        void handleDelete(record.id);
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
    ], [openEditModal, handleDelete]);

    return (
        <div>
            <Typography.Title level={3}>执行器管理</Typography.Title>

            {/* 搜索区域 */}
            <div className="simple-search-panel">
                <Space wrap>
                    <Input
                        placeholder="关键字（编码/名称）"
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
                        options={[{label: "启用", value: "ENABLE"}, {label: "停用", value: "DISABLE"}]}
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
                <Space>
                    <Button icon={<BookOutlined/>} loading={protocolLoading} onClick={handleOpenProtocol}>协议说明</Button>
                    <Button type="primary" onClick={openCreateModal}>新增执行器</Button>
                </Space>
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
                title={editingId ? "编辑执行器" : "新增执行器"}
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
                    <Form form={form} layout="horizontal" size="middle" labelCol={{span: 5}} wrapperCol={{span: 19}}>
                        <Form.Item
                            label="执行器编码"
                            name="executorCode"
                            rules={[{required: true, message: "请输入执行器编码"}]}
                            style={{marginBottom: 16}}
                        >
                            <Input style={{height: 36, fontSize: 14, padding: "0 12px"}} placeholder="如 WINDOWS_RPA"/>
                        </Form.Item>
                        <Form.Item
                            label="执行器名称"
                            name="executorName"
                            rules={[{required: true, message: "请输入执行器名称"}]}
                            style={{marginBottom: 16}}
                        >
                            <Input style={{height: 36, fontSize: 14, padding: "0 12px"}} placeholder="如 Windows RPA 执行器"/>
                        </Form.Item>
                        <Form.Item label="描述" name="description" style={{marginBottom: 16}}>
                            <Input.TextArea rows={3}/>
                        </Form.Item>
                        <Form.Item label="状态" name="status" style={{marginBottom: 16}}>
                            <Select
                                placeholder="请选择状态"
                                style={{height: 36}}
                                options={[
                                    {label: "启用", value: "ENABLE"},
                                    {label: "停用", value: "DISABLE"}
                                ]}
                            />
                        </Form.Item>
                        <Form.Item label="备注" name="remark" style={{marginBottom: 0}}>
                            <Input.TextArea rows={3}/>
                        </Form.Item>
                    </Form>
                </div>
            </Modal>

            {/* 协议说明弹窗 */}
            <Modal
                open={showProtocolModal}
                onCancel={() => setShowProtocolModal(false)}
                title={
                    <Space>
                        <BookOutlined/>
                        <span>{protocolData?.protocolName} {protocolData?.protocolVersion} - 协议说明</span>
                    </Space>
                }
                width={960}
                centered
                footer={<Button onClick={() => setShowProtocolModal(false)}>关闭</Button>}
            >
                {protocolData && (
                    <div style={{maxHeight: "70vh", overflowY: "auto", padding: "8px 0"}}>
                        {/* 协议概述 */}
                        <Typography.Title level={5}>协议概述</Typography.Title>
                        <Descriptions column={2} size="small" bordered style={{marginBottom: 16}}>
                            <Descriptions.Item label="协议名称">{protocolData.protocolName}</Descriptions.Item>
                            <Descriptions.Item label="协议版本">{protocolData.protocolVersion}</Descriptions.Item>
                        </Descriptions>
                        <Typography.Paragraph style={{marginBottom: 16, whiteSpace: "pre-wrap"}}>
                            {protocolData.description}
                        </Typography.Paragraph>

                        {/* 消息结构 */}
                        <Typography.Title level={5}>外层消息结构</Typography.Title>
                        <Typography.Paragraph style={{marginBottom: 8, whiteSpace: "pre-wrap"}}>
                            {protocolData.outerStructure.description}
                        </Typography.Paragraph>
                        <Typography.Paragraph
                            style={{
                                marginBottom: 12,
                                background: "#f5f5f5",
                                padding: 12,
                                borderRadius: 4,
                                fontFamily: "monospace",
                                fontSize: 12,
                                whiteSpace: "pre-wrap"
                            }}
                        >
                            {protocolData.outerStructure.jsonExample}
                        </Typography.Paragraph>
                        <Table
                            rowKey="name"
                            size="small"
                            bordered
                            pagination={false}
                            dataSource={protocolData.outerStructure.fields}
                            columns={[
                                {title: "字段名", dataIndex: "name", width: 160},
                                {title: "类型", dataIndex: "type", width: 120},
                                {title: "必填", dataIndex: "required", width: 60, render: (v: boolean) => v ? "是" : "否"},
                                {title: "说明", dataIndex: "description"}
                            ]}
                            style={{marginBottom: 16}}
                        />

                        {/* 消息类型 */}
                        <Typography.Title level={5}>消息类型</Typography.Title>
                        <Collapse
                            items={protocolData.messageTypes.map((mt, idx) => ({
                                key: idx,
                                label: (
                                    <Space>
                                        <Tag color="blue">{mt.typeName}</Tag>
                                        <Tag>{mt.direction}</Tag>
                                        <Typography.Text type="secondary" style={{fontSize: 12}}>{mt.description.substring(0, 60)}...</Typography.Text>
                                    </Space>
                                ),
                                children: (
                                    <div>
                                        <Typography.Paragraph style={{whiteSpace: "pre-wrap"}}>{mt.description}</Typography.Paragraph>
                                        <Typography.Paragraph
                                            style={{
                                                marginBottom: 12,
                                                background: "#f5f5f5",
                                                padding: 12,
                                                borderRadius: 4,
                                                fontFamily: "monospace",
                                                fontSize: 12,
                                                whiteSpace: "pre-wrap"
                                            }}
                                        >
                                            {mt.jsonExample}
                                        </Typography.Paragraph>
                                        <Table
                                            rowKey="name"
                                            size="small"
                                            bordered
                                            pagination={false}
                                            dataSource={mt.fields}
                                            columns={[
                                                {title: "字段名", dataIndex: "name", width: 200},
                                                {title: "类型", dataIndex: "type", width: 160},
                                                {title: "必填", dataIndex: "required", width: 60, render: (v: boolean) => v ? "是" : "否"},
                                                {title: "说明", dataIndex: "description"}
                                            ]}
                                        />
                                    </div>
                                )
                            }))}
                            style={{marginBottom: 16}}
                        />

                        {/* 内置系统命令 */}
                        <Typography.Title level={5}>内置系统命令</Typography.Title>
                        <Collapse
                            items={protocolData.systemCommands.map((sc, idx) => ({
                                key: idx,
                                label: (
                                    <Space>
                                        <Tag color="green">{sc.commandCode}</Tag>
                                        <Typography.Text type="secondary" style={{fontSize: 12}}>{sc.description.substring(0, 60)}...</Typography.Text>
                                    </Space>
                                ),
                                children: (
                                    <div>
                                        <Typography.Paragraph style={{whiteSpace: "pre-wrap"}}>{sc.description}</Typography.Paragraph>
                                        <Descriptions column={1} size="small" bordered style={{marginBottom: 12}}>
                                            <Descriptions.Item label="返回数据">{sc.resultDescription}</Descriptions.Item>
                                        </Descriptions>
                                        {sc.args.length > 0 && (
                                            <>
                                                <Typography.Text strong style={{display: "block", marginBottom: 8}}>参数字段</Typography.Text>
                                                <Table
                                                    rowKey="name"
                                                    size="small"
                                                    bordered
                                                    pagination={false}
                                                    dataSource={sc.args}
                                                    columns={[
                                                        {title: "字段名", dataIndex: "name", width: 160},
                                                        {title: "类型", dataIndex: "type", width: 100},
                                                        {title: "必填", dataIndex: "required", width: 60, render: (v: boolean) => v ? "是" : "否"},
                                                        {title: "说明", dataIndex: "description"}
                                                    ]}
                                                    style={{marginBottom: 12}}
                                                />
                                            </>
                                        )}
                                        <Typography.Paragraph
                                            style={{
                                                background: "#f5f5f5",
                                                padding: 12,
                                                borderRadius: 4,
                                                fontFamily: "monospace",
                                                fontSize: 12,
                                                whiteSpace: "pre-wrap"
                                            }}
                                        >
                                            {sc.jsonExample}
                                        </Typography.Paragraph>
                                    </div>
                                )
                            }))}
                            style={{marginBottom: 16}}
                        />

                        {/* 通信流程 */}
                        <Typography.Title level={5}>通信流程</Typography.Title>
                        <Typography.Paragraph style={{whiteSpace: "pre-wrap", background: "#fafafa", padding: 12, borderRadius: 4}}>
                            {protocolData.communicationFlow}
                        </Typography.Paragraph>
                    </div>
                )}
            </Modal>
        </div>
    );
}
