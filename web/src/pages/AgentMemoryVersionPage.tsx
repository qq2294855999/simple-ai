import type {MenuProps} from "antd";
import {Button, Dropdown, Form, Input, Modal, Select, Space, Table, Tag, Tooltip, Typography} from "antd";
import type {ColumnsType} from "antd/es/table";
import {MoreOutlined} from "@ant-design/icons";
import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {usePreventDoubleClickHook} from "../hooks/usePreventDoubleClickHook";
import {ToastUtil} from "../utils/ToastUtil";
import {AgentMemoryVersionApi} from "../api/agentMemoryVersionApi";
import type {
    AgentMemoryVersionPageResponseDto,
    CreateAgentMemoryVersionRequestDto,
    UpdateAgentMemoryVersionRequestDto
} from "../dto/agentMemoryVersion/AgentMemoryVersionDto";

/**
 * 获取版本状态中文标签。
 *
 * @param status 版本状态码
 * @returns 状态中文
 */
function getVersionStatusLabel(status: string): string {
    if (status === "DRAFT") return "草稿";
    if (status === "PUBLISHED") return "已发布";
    if (status === "RETIRED") return "已退役";
    return status || "-";
}

/**
 * 获取版本状态颜色。
 */
function getVersionStatusColor(status: string): string {
    if (status === "DRAFT") return "default";
    if (status === "PUBLISHED") return "green";
    if (status === "RETIRED") return "red";
    return "default";
}

/**
 * 记忆版本管理页面。
 *
 * @author qty
 */
export function AgentMemoryVersionPage() {
    const [dataSource, setDataSource] = useState<AgentMemoryVersionPageResponseDto[]>([]);
    const [loading, setLoading] = useState(false);
    const [total, setTotal] = useState(0);
    const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

    // 搜索条件
    const [keyword, setKeyword] = useState("");
    const [filterVersionStatus, setFilterVersionStatus] = useState<string | undefined>(undefined);
    const [pageIndex, setPageIndex] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 弹窗状态
    const [showModal, setShowModal] = useState(false);
    const [editingId, setEditingId] = useState<string | null>(null);
    const [form] = Form.useForm();

    // 加载列表数据
    const loadDataRef = useRef<(() => Promise<void>) | null>(null);
    const loadData = useCallback(async () => {
        setLoading(true);
        try {
            const result = await AgentMemoryVersionApi.page({
                current: pageIndex,
                size: pageSize,
                keyword: keyword || undefined,
                versionStatus: filterVersionStatus
            });
            setDataSource(result.records || []);
            setTotal(result.total || 0);
        } finally {
            setLoading(false);
        }
    }, [pageIndex, pageSize, keyword, filterVersionStatus]);

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
        setFilterVersionStatus(undefined);
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
            const record = await AgentMemoryVersionApi.findOne(id);
            form.setFieldsValue({
                memoryId: record.memoryId,
                versionNo: record.versionNo,
                versionStatus: record.versionStatus,
                sourceTaskId: record.sourceTaskId,
                summary: record.summary,
                createReason: record.createReason
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
            const updateData: UpdateAgentMemoryVersionRequestDto = {id: editingId, ...values};
            await AgentMemoryVersionApi.update(editingId, updateData);
            ToastUtil.success("更新成功");
        } else {
            const createData: CreateAgentMemoryVersionRequestDto = values;
            await AgentMemoryVersionApi.create(createData);
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
        await AgentMemoryVersionApi.deleteByIds(selectedRowKeys);
        ToastUtil.success("删除成功");
        setSelectedRowKeys([]);
        loadDataRef.current?.();
    });

    // 删除单个
    const handleDelete = useCallback(async (id: string) => {
        await AgentMemoryVersionApi.deleteByIds([id]);
        ToastUtil.success("删除成功");
        loadDataRef.current?.();
    }, []);

    // 发布
    const handlePublish = useCallback(async (id: string) => {
        await AgentMemoryVersionApi.publish(id);
        ToastUtil.success("已发布");
        loadDataRef.current?.();
    }, []);

    // 废弃
    const handleRetire = useCallback(async (id: string) => {
        await AgentMemoryVersionApi.retire(id);
        ToastUtil.success("已废弃");
        loadDataRef.current?.();
    }, []);

    // 表格列定义
    const columns = useMemo<ColumnsType<AgentMemoryVersionPageResponseDto>>(() => [
        {title: "记忆ID", dataIndex: "memoryId", width: 180, ellipsis: true, render: value => <Tooltip title={value}>{value}</Tooltip>},
        {title: "版本号", dataIndex: "versionNo", width: 80},
        {
            title: "版本状态",
            dataIndex: "versionStatus",
            width: 100,
            render: (status: string) => <Tag color={getVersionStatusColor(status)}>{getVersionStatusLabel(status)}</Tag>
        },
        {title: "来源任务", dataIndex: "sourceTaskId", width: 180, ellipsis: true, render: value => <Tooltip title={value}>{value || "-"}</Tooltip>},
        {title: "版本摘要", dataIndex: "summary", width: 200, ellipsis: true, render: value => <Tooltip title={value}>{value || "-"}</Tooltip>},
        {title: "创建原因", dataIndex: "createReason", width: 200, ellipsis: true, render: value => <Tooltip title={value}>{value || "-"}</Tooltip>},
        {title: "创建时间", dataIndex: "createTime", width: 160},
        {
            title: "操作", width: 150,
            render: (_: unknown, record: AgentMemoryVersionPageResponseDto) => {
                const isDraft = record.versionStatus === "DRAFT";
                const isPublished = record.versionStatus === "PUBLISHED";
                const menuItems: MenuProps["items"] = [
                    ...(isDraft ? [{key: "publish", label: "发布"}] : []),
                    ...(isPublished ? [{key: "retire", label: "废弃"}] : []),
                    {key: "delete", label: "删除", danger: true}
                ];
                const handleMenuAction = (key: string) => {
                    if (key === "publish") {
                        void handlePublish(record.id);
                    }
                    if (key === "retire") {
                        void handleRetire(record.id);
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
    ], [openEditModal, handleDelete, handlePublish, handleRetire]);

    return (
        <div>
            <Typography.Title level={3}>记忆版本管理</Typography.Title>

            {/* 搜索区域 */}
            <div className="simple-search-panel">
                <Space wrap>
                    <Input
                        placeholder="关键字（摘要/原因）"
                        value={keyword}
                        onChange={e => setKeyword(e.target.value)}
                        style={{width: 200, height: 36}}
                        allowClear
                    />
                    <Select
                        placeholder="版本状态"
                        value={filterVersionStatus}
                        onChange={setFilterVersionStatus}
                        style={{width: 130, height: 36}}
                        allowClear
                        options={[
                            {label: "草稿", value: "DRAFT"},
                            {label: "已发布", value: "PUBLISHED"},
                            {label: "已退役", value: "RETIRED"}
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
                <Button type="primary" onClick={openCreateModal}>新增版本</Button>
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
                title={editingId ? "编辑记忆版本" : "新增记忆版本"}
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
                            label="记忆ID"
                            name="memoryId"
                            rules={[{required: true, message: "请输入记忆ID"}]}
                            style={{marginBottom: 16}}
                        >
                            <Input style={{height: 36, fontSize: 14, padding: "0 12px"}} placeholder="记忆主键"/>
                        </Form.Item>
                        <Form.Item
                            label="版本号"
                            name="versionNo"
                            rules={[{required: true, message: "请输入版本号"}]}
                            style={{marginBottom: 16}}
                        >
                            <Input type="number" style={{height: 36, fontSize: 14, padding: "0 12px"}} placeholder="如 1"/>
                        </Form.Item>
                        <Form.Item label="版本状态" name="versionStatus" style={{marginBottom: 16}}>
                            <Select
                                placeholder="选择状态（可选）"
                                style={{height: 36}}
                                allowClear
                                options={[
                                    {label: "草稿", value: "DRAFT"},
                                    {label: "已发布", value: "PUBLISHED"},
                                    {label: "已退役", value: "RETIRED"}
                                ]}
                            />
                        </Form.Item>
                        <Form.Item label="来源任务ID" name="sourceTaskId" style={{marginBottom: 16}}>
                            <Input style={{height: 36, fontSize: 14, padding: "0 12px"}} placeholder="可选"/>
                        </Form.Item>
                        <Form.Item label="版本摘要" name="summary" style={{marginBottom: 16}}>
                            <Input.TextArea rows={3}/>
                        </Form.Item>
                        <Form.Item label="创建原因" name="createReason" style={{marginBottom: 0}}>
                            <Input.TextArea rows={3}/>
                        </Form.Item>
                    </Form>
                </div>
            </Modal>
        </div>
    );
}
