import type {MenuProps} from "antd";
import {Button, Dropdown, Input, Select, Space, Table, Tag, Typography} from "antd";
import type {ColumnsType} from "antd/es/table";
import {MoreOutlined} from "@ant-design/icons";
import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {useNavigate} from "react-router-dom";
import {usePreventDoubleClickHook} from "../hooks/usePreventDoubleClickHook";
import {ToastUtil} from "../utils/ToastUtil";
import {ProtocolApi} from "../api/protocolApi";
import type {ProtocolPageResponseDto} from "../dto/protocol/ProtocolDto";

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
 * 执行器协议管理列表页面。
 * 新增/编辑/查看均跳转到独立页面，不再使用弹窗。
 *
 * @author qty
 */
export function ProtocolManagementPage() {
    const navigate = useNavigate();
    const [dataSource, setDataSource] = useState<ProtocolPageResponseDto[]>([]);
    const [loading, setLoading] = useState(false);
    const [total, setTotal] = useState(0);
    const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

    // 搜索条件
    const [keyword, setKeyword] = useState("");
    const [filterStatus, setFilterStatus] = useState<string | undefined>(undefined);
    const [pageIndex, setPageIndex] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 加载列表数据
    const loadDataRef = useRef<(() => Promise<void>) | null>(null);
    const loadData = useCallback(async () => {
        setLoading(true);
        try {
            const result = await ProtocolApi.page({
                current: pageIndex,
                size: pageSize,
                protocolCode: keyword || undefined,
                protocolName: keyword || undefined,
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

    // 切换启用/停用状态
    const handleToggleStatus = useCallback(async (id: string, currentStatus: string) => {
        try {
            const newStatus = await ProtocolApi.toggleStatus(id);
            ToastUtil.success(`协议已${newStatus === "ON" ? "启用" : "停用"}`);
            loadDataRef.current?.();
        } catch {
            ToastUtil.error("状态切换失败");
        }
    }, []);

    // 批量删除
    const {onClick: handleBatchDelete, loading: batchDeleteLoading} = usePreventDoubleClickHook(async () => {
        if (selectedRowKeys.length === 0) {
            ToastUtil.error("请先选择要删除的记录");
            return;
        }
        await ProtocolApi.deleteByIds(selectedRowKeys);
        ToastUtil.success("删除成功");
        setSelectedRowKeys([]);
        loadDataRef.current?.();
    });

    // 删除单个
    const handleDelete = useCallback(async (id: string) => {
        await ProtocolApi.deleteByIds([id]);
        ToastUtil.success("删除成功");
        loadDataRef.current?.();
    }, []);

    // 表格列定义
    const columns = useMemo<ColumnsType<ProtocolPageResponseDto>>(() => [
        {title: "协议名称", dataIndex: "protocolName", width: 180},
        {title: "协议编码", dataIndex: "protocolCode", width: 140},
        {title: "协议版本", dataIndex: "protocolVersion", width: 100},
        {
            title: "状态",
            dataIndex: "status",
            width: 80,
            render: (status: string) => <Tag color={getStatusLabel(status) === "启用" ? "green" : "red"}>{getStatusLabel(status)}</Tag>
        },
        {title: "创建时间", dataIndex: "createTime", width: 160},
        {title: "修改时间", dataIndex: "updateTime", width: 160},
        {
            title: "操作", width: 180,
            render: (_: unknown, record: ProtocolPageResponseDto) => {
                const isEnabled = getStatusLabel(record.status) === "启用";
                const menuItems: MenuProps["items"] = [
                    {key: "view", label: "查看"},
                    {key: "enable", label: isEnabled ? "停用" : "启用"},
                    {key: "delete", label: "删除", danger: true}
                ];
                const handleMenuAction = (key: string) => {
                    if (key === "view") {
                        navigate(`/agent-protocol/${record.id}`);
                    }
                    if (key === "enable") {
                        void handleToggleStatus(record.id, record.status);
                    }
                    if (key === "delete") {
                        void handleDelete(record.id);
                    }
                };
                return (
                    <Space>
                        <Button type="link" size="small" onClick={() => navigate(`/agent-protocol/${record.id}/edit`)}>编辑</Button>
                        <Dropdown menu={{items: menuItems, onClick: ({key}) => handleMenuAction(key)}}>
                            <Button type="link" size="small" icon={<MoreOutlined/>}>更多</Button>
                        </Dropdown>
                    </Space>
                );
            }
        }
    ], [navigate, handleDelete, handleToggleStatus]);

    return (
        <div>
            <Typography.Title level={3}>协议管理</Typography.Title>

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
                        options={[{label: "启用", value: "ON"}, {label: "停用", value: "OFF"}]}
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
                    <Button type="primary" onClick={() => navigate("/agent-protocol/create")}>新增协议</Button>
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
        </div>
    );
}