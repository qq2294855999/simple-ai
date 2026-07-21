import type {MenuProps} from "antd";
import {Button, Descriptions, Dropdown, Form, Input, Modal, Select, Space, Table, Tag, Tooltip, Typography} from "antd";
import type {ColumnsType} from "antd/es/table";
import {CopyOutlined, MoreOutlined} from "@ant-design/icons";
import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {useSearchParams} from "react-router-dom";
import {usePreventDoubleClickHook} from "../hooks/usePreventDoubleClickHook";
import {ToastUtil} from "../utils/ToastUtil";
import {AgentClientApi} from "../api/agentClientApi";
import {AgentExecutorApi} from "../api/agentExecutorApi";
import type {
    AgentClientCreateResponseDto,
    AgentClientPageResponseDto,
    CreateAgentClientRequestDto,
    UpdateAgentClientRequestDto
} from "../dto/agentClient/AgentClientDto";

/**
 * 获取状态中文标签。
 *
 * @param status 状态码或状态名
 * @returns 状态中文
 */
function getStatusLabel(status: number | string): string {
    if (status === 1 || status === "ACTIVE") return "启用";
    if (status === 2 || status === "EXPIRED") return "已过期";
    if (status === "DISABLED") return "已停用";
    if (status === "REVOKED") return "已吊销";
    return String(status) || "-";
}

/**
 * 获取状态颜色。
 */
function getStatusColor(status: string): string {
    if (status === "ACTIVE") return "green";
    if (status === "EXPIRED") return "orange";
    if (status === "DISABLED") return "red";
    if (status === "REVOKED") return "red";
    return "default";
}

/**
 * 过期时间单位选项。
 */
const EXPIRE_UNIT_OPTIONS = [
    {label: "天", value: "DAY"},
    {label: "周", value: "WEEK"},
    {label: "月", value: "MONTH"},
    {label: "年", value: "YEAR"}
];

/**
 * 客户端实例管理页面。
 *
 * @author qty
 */
export function AgentClientManagementPage() {
    const [searchParams] = useSearchParams();
    const [dataSource, setDataSource] = useState<AgentClientPageResponseDto[]>([]);
    const [loading, setLoading] = useState(false);
    const [total, setTotal] = useState(0);
    const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

    // 搜索条件
    const [keyword, setKeyword] = useState("");
    const [filterExecutorId, setFilterExecutorId] = useState<string | undefined>(undefined);
    const [filterStatus, setFilterStatus] = useState<string | undefined>(undefined);
    const [pageIndex, setPageIndex] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 弹窗状态
    const [showModal, setShowModal] = useState(false);
    const [editingId, setEditingId] = useState<string | null>(null);
    const [form] = Form.useForm();

    // 密钥展示弹窗
    const [showSecretModal, setShowSecretModal] = useState(false);
    const [secretData, setSecretData] = useState<AgentClientCreateResponseDto | null>(null);

    // 执行器下拉数据
    const [executors, setExecutors] = useState<{ id: string; name: string }[]>([]);

    // 加载执行器下拉
    const loadExecutors = useCallback(async () => {
        try {
            const result = await AgentExecutorApi.page({current: 1, size: 1000});
            const list = (result.records || [])
                .filter((a: { executorName?: string }) => a.executorName)
                .map((a: { id: string; executorName: string }) => ({id: a.id, name: a.executorName}));
            setExecutors(list);
        } catch {
            // 下拉加载失败不影响主流程
        }
    }, []);

    // 加载列表数据
    const loadDataRef = useRef<(() => Promise<void>) | null>(null);
    const loadData = useCallback(async () => {
        setLoading(true);
        try {
            const result = await AgentClientApi.page({
                current: pageIndex,
                size: pageSize,
                keyword: keyword || undefined,
                executorId: filterExecutorId,
                status: filterStatus
            });
            setDataSource(result.records || []);
            setTotal(result.total || 0);
        } finally {
            setLoading(false);
        }
    }, [pageIndex, pageSize, keyword, filterExecutorId, filterStatus]);

    loadDataRef.current = loadData;

    useEffect(() => {
        loadData();
        loadExecutors().then(() => {
            // 从执行器页面跳转过来时，自动打开创建弹窗并选中执行器
            const executorId = searchParams.get("executorId");
            if (executorId) {
                form.setFieldsValue({executorId});
                setShowModal(true);
            }
        });
    }, [loadData, loadExecutors, searchParams, form]);

    // 搜索
    const handleSearch = useCallback(() => {
        setPageIndex(1);
        loadDataRef.current?.();
    }, []);

    // 重置搜索
    const handleReset = useCallback(() => {
        setKeyword("");
        setFilterExecutorId(undefined);
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
            const record = await AgentClientApi.findOne(id);
            form.setFieldsValue({
                clientName: record.clientName,
                executorId: record.executorId,
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
            const updateData: UpdateAgentClientRequestDto = {id: editingId, ...values};
            await AgentClientApi.update(editingId, updateData);
            ToastUtil.success("更新成功");
        } else {
            const createData: CreateAgentClientRequestDto = {
                executorId: values.executorId,
                clientName: values.clientName,
                expireDuration: values.expireDuration,
                expireUnit: values.expireUnit,
                remark: values.remark
            };
            const result = await AgentClientApi.create(createData);
            ToastUtil.success("创建成功");

            // 显示含明文密钥的弹窗
            setSecretData(result);
            setShowSecretModal(true);
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
        await AgentClientApi.deleteByIds(selectedRowKeys);
        ToastUtil.success("删除成功");
        setSelectedRowKeys([]);
        loadDataRef.current?.();
    });

    // 删除单个
    const handleDelete = useCallback(async (id: string) => {
        await AgentClientApi.deleteByIds([id]);
        ToastUtil.success("删除成功");
        loadDataRef.current?.();
    }, []);

    // 复制文本
    const handleCopy = useCallback(async (text: string) => {
        try {
            await navigator.clipboard.writeText(text);
            ToastUtil.success("已复制到剪贴板");
        } catch {
            ToastUtil.error("复制失败，请手动复制");
        }
    }, []);

    // 表格列定义
    const columns = useMemo<ColumnsType<AgentClientPageResponseDto>>(() => [
        {title: "客户端名称", dataIndex: "clientName", width: 140},
        {title: "执行器", dataIndex: "executorName", width: 140, render: value => value || "-"},
        {title: "机器名称", dataIndex: "machineName", width: 140, render: value => value || "-"},
        {title: "状态", dataIndex: "status", width: 90, render: (status: string) => <Tag color={getStatusColor(status)}>{getStatusLabel(status)}</Tag>},
        {title: "过期时间", dataIndex: "expireTime", width: 160},
        {title: "最近连接", dataIndex: "lastConnectedAt", width: 160, render: value => value || "-"},
        {title: "备注", dataIndex: "remark", ellipsis: true, render: value => <Tooltip title={value}>{value || "-"}</Tooltip>},
        {title: "创建时间", dataIndex: "createTime", width: 160},
        {
            title: "操作", width: 150,
            render: (_: unknown, record: AgentClientPageResponseDto) => {
                const menuItems: MenuProps["items"] = [
                    {key: "delete", label: "删除", danger: true}
                ];
                const handleMenuAction = (key: string) => {
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
            <Typography.Title level={3}>客户端管理</Typography.Title>

            {/* 搜索区域 */}
            <div className="simple-search-panel">
                <Space wrap>
                    <Input
                        placeholder="关键字（客户端名称）"
                        value={keyword}
                        onChange={e => setKeyword(e.target.value)}
                        style={{width: 200, height: 36}}
                        allowClear
                    />
                    <Select
                        placeholder="执行器"
                        value={filterExecutorId}
                        onChange={setFilterExecutorId}
                        style={{width: 160, height: 36}}
                        allowClear
                        options={executors.map(a => ({label: a.name, value: a.id}))}
                    />
                    <Select
                        placeholder="状态"
                        value={filterStatus}
                        onChange={setFilterStatus}
                        style={{width: 120, height: 36}}
                        allowClear
                        options={[
                            {label: "启用", value: "ACTIVE"},
                            {label: "已过期", value: "EXPIRED"},
                            {label: "已停用", value: "DISABLED"},
                            {label: "已吊销", value: "REVOKED"}
                        ]}
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
                <Button type="primary" onClick={openCreateModal}>新增客户端</Button>
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
                title={editingId ? "编辑客户端" : "新增客户端"}
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
                    <Form form={form} layout="horizontal" size="middle" labelCol={{span: 6}} wrapperCol={{span: 18}}>
                        <Form.Item
                            label="客户端名称"
                            name="clientName"
                            rules={[{required: true, message: "请输入客户端名称"}]}
                            style={{marginBottom: 16}}
                        >
                            <Input style={{height: 36, fontSize: 14, padding: "0 12px"}} placeholder="如 办公电脑"/>
                        </Form.Item>
                        <Form.Item
                            label="执行器"
                            name="executorId"
                            rules={[{required: true, message: "请选择执行器"}]}
                            style={{marginBottom: 16}}
                        >
                            <Select
                                placeholder="选择执行器"
                                style={{height: 36}}
                                options={executors.map(a => ({label: a.name, value: a.id}))}
                                showSearch
                                filterOption={(input, option) => (option?.label as string || "").includes(input)}
                            />
                        </Form.Item>
                        <Form.Item label="过期时长" style={{marginBottom: 16}}>
                            <Space>
                                <Form.Item name="expireDuration" noStyle>
                                    <Input type="number" style={{width: 120, height: 36}} placeholder="数字" min={1}/>
                                </Form.Item>
                                <Form.Item name="expireUnit" noStyle>
                                    <Select style={{width: 100, height: 36}} placeholder="单位" options={EXPIRE_UNIT_OPTIONS}/>
                                </Form.Item>
                            </Space>
                        </Form.Item>
                        <Form.Item label="备注" name="remark" style={{marginBottom: 0}}>
                            <Input.TextArea rows={3}/>
                        </Form.Item>
                    </Form>
                </div>
            </Modal>

            {/* 密钥展示弹窗 */}
            <Modal
                open={showSecretModal}
                onCancel={() => setShowSecretModal(false)}
                title="客户端创建成功"
                width={600}
                centered
                footer={[
                    <Button key="close" type="primary" onClick={() => setShowSecretModal(false)}>我已保存</Button>
                ]}
            >
                <div style={{padding: "16px 0"}}>
                    <Typography.Text type="warning" strong style={{fontSize: 14, display: "block", marginBottom: 12}}>
                        客户端密钥仅在创建时展示一次，关闭后将无法再次查看，请立即复制并妥善保存！
                    </Typography.Text>
                    {secretData && (
                        <Descriptions column={1} bordered size="small">
                            <Descriptions.Item label="客户端ID">{secretData.id}</Descriptions.Item>
                            <Descriptions.Item label="客户端名称">{secretData.clientName}</Descriptions.Item>
                            <Descriptions.Item label="过期时间">{secretData.expireTime}</Descriptions.Item>
                            <Descriptions.Item label="客户端密钥">
                                <Space>
                                    <Typography.Text code copyable style={{fontSize: 13}}>{secretData.clientSecret}</Typography.Text>
                                    <Button
                                        type="link"
                                        icon={<CopyOutlined/>}
                                        size="small"
                                        onClick={() => handleCopy(secretData.clientSecret)}
                                    >
                                        复制
                                    </Button>
                                </Space>
                            </Descriptions.Item>
                        </Descriptions>
                    )}
                </div>
            </Modal>
        </div>
    );
}
