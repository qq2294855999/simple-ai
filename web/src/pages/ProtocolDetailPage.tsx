import {Button, Descriptions, Space, Spin, Tag, Typography} from "antd";
import {ArrowLeftOutlined, EditOutlined} from "@ant-design/icons";
import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import {ToastUtil} from "../utils/ToastUtil";
import {ProtocolApi} from "../api/protocolApi";
import {RestrictedMarkdownComponent} from "../components/agentChat/RestrictedMarkdownComponent";
import type {ProtocolInfoResponseDto} from "../dto/protocol/ProtocolDto";

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
 * 执行器协议详情只读页面。
 * 上方 Descriptions 展示元信息，下方 react-markdown 渲染协议内容。
 *
 * @author qty
 */
export function ProtocolDetailPage() {
    const navigate = useNavigate();
    const {id} = useParams<{ id: string }>();

    const [record, setRecord] = useState<ProtocolInfoResponseDto | null>(null);
    const [pageLoading, setPageLoading] = useState(false);

    // 加载详情数据
    useEffect(() => {
        if (!id) return;
        setPageLoading(true);
        ProtocolApi.findOne(id)
            .then(setRecord)
            .catch(() => {
                ToastUtil.error("获取协议详情失败");
            })
            .finally(() => {
                setPageLoading(false);
            });
    }, [id]);

    return (
        <Spin spinning={pageLoading}>
            <div>
                {/* 顶部导航 */}
                <div style={{marginBottom: 16}}>
                    <Space>
                        <Button
                            type="text"
                            icon={<ArrowLeftOutlined/>}
                            onClick={() => navigate("/agent-protocol")}
                        >
                            返回列表
                        </Button>
                        <Typography.Text type="secondary">协议管理 &gt; 协议详情</Typography.Text>
                    </Space>
                </div>

                <div style={{display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16}}>
                    <Typography.Title level={3} style={{margin: 0}}>协议详情</Typography.Title>
                    <Button
                        type="primary"
                        icon={<EditOutlined/>}
                        onClick={() => navigate(`/agent-protocol/${id}/edit`)}
                    >
                        编辑
                    </Button>
                </div>

                {/* 元信息 */}
                {record && (
                    <div className="simple-search-panel">
                        <Descriptions column={3} bordered size="small">
                            <Descriptions.Item label="协议名称">{record.protocolName}</Descriptions.Item>
                            <Descriptions.Item label="协议编码">{record.protocolCode}</Descriptions.Item>
                            <Descriptions.Item label="协议版本">{record.protocolVersion}</Descriptions.Item>
                            <Descriptions.Item label="状态">
                                <Tag color={getStatusLabel(record.status) === "启用" ? "green" : "red"}>
                                    {getStatusLabel(record.status)}
                                </Tag>
                            </Descriptions.Item>
                            <Descriptions.Item label="创建时间">{record.createTime}</Descriptions.Item>
                            <Descriptions.Item label="修改时间">{record.updateTime}</Descriptions.Item>
                        </Descriptions>
                    </div>
                )}

                {/* Markdown 渲染区 */}
                <Typography.Title level={5} style={{marginTop: 24, marginBottom: 8}}>协议内容</Typography.Title>
                <div
                    className="simple-search-panel"
                    style={{
                        padding: 24,
                        minHeight: 300,
                        background: "#fafafa"
                    }}
                >
                    {record?.content ? (
                        <RestrictedMarkdownComponent content={record.content}/>
                    ) : (
                        <Typography.Text type="secondary">暂无协议内容</Typography.Text>
                    )}
                </div>
            </div>
        </Spin>
    );
}