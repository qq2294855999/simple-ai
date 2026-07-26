import {Button, Descriptions, Space, Spin, Tag, Typography} from "antd";
import {ArrowLeftOutlined, EditOutlined} from "@ant-design/icons";
import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import {ToastUtil} from "../utils/ToastUtil";
import {AgentRuleApi} from "../api/agentRuleApi";
import {RestrictedMarkdownComponent} from "../components/agentChat/RestrictedMarkdownComponent";
import type {AgentRuleInfoResponseDto} from "../dto/agentRule/AgentRuleDto";

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
 * 智能体规则详情只读页面。
 * 触发条件和定义描述以 Markdown 渲染展示，上下排列。
 *
 * @author qty
 */
export function AgentRuleDetailPage() {
    const navigate = useNavigate();
    const {id} = useParams<{ id: string }>();

    const [record, setRecord] = useState<AgentRuleInfoResponseDto | null>(null);
    const [pageLoading, setPageLoading] = useState(false);

    // 加载详情数据
    useEffect(() => {
        if (!id) return;
        setPageLoading(true);
        AgentRuleApi.findOne(id)
            .then(setRecord)
            .catch(() => {
                ToastUtil.error("获取规则详情失败");
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
                            onClick={() => navigate("/agent-rule")}
                        >
                            返回列表
                        </Button>
                        <Typography.Text type="secondary">规则管理 &gt; 规则详情</Typography.Text>
                    </Space>
                </div>

                <div style={{display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16}}>
                    <Typography.Title level={3} style={{margin: 0}}>规则详情</Typography.Title>
                    <Button
                        type="primary"
                        icon={<EditOutlined/>}
                        onClick={() => navigate(`/agent-rule/${id}/edit`)}
                    >
                        编辑
                    </Button>
                </div>

                {/* 元信息 */}
                {record && (
                    <div className="simple-search-panel">
                        <Descriptions column={3} bordered size="small">
                            <Descriptions.Item label="触发动作">{record.triggerAction}</Descriptions.Item>
                            <Descriptions.Item label="状态">
                                <Tag color={getStatusLabel(record.status ?? "") === "启用" ? "green" : "red"}>
                                    {getStatusLabel(record.status ?? "")}
                                </Tag>
                            </Descriptions.Item>
                            <Descriptions.Item label="创建时间">{record.createTime}</Descriptions.Item>
                            <Descriptions.Item label="修改时间">{record.updateTime}</Descriptions.Item>
                            {record.remark && (
                                <Descriptions.Item label="备注">{record.remark}</Descriptions.Item>
                            )}
                        </Descriptions>
                    </div>
                )}

                {/* 触发条件 Markdown 渲染区 */}
                <Typography.Title level={5} style={{marginTop: 24, marginBottom: 8}}>触发条件</Typography.Title>
                <div
                    className="simple-search-panel"
                    style={{
                        padding: 24,
                        minHeight: 200,
                        background: "#fafafa"
                    }}
                >
                    {record?.triggerCondition ? (
                        <RestrictedMarkdownComponent content={record.triggerCondition}/>
                    ) : (
                        <Typography.Text type="secondary">暂无触发条件</Typography.Text>
                    )}
                </div>

                {/* 定义描述 Markdown 渲染区 */}
                <Typography.Title level={5} style={{marginTop: 24, marginBottom: 8}}>定义描述</Typography.Title>
                <div
                    className="simple-search-panel"
                    style={{
                        padding: 24,
                        minHeight: 200,
                        background: "#fafafa"
                    }}
                >
                    {record?.definitionDesc ? (
                        <RestrictedMarkdownComponent content={record.definitionDesc}/>
                    ) : (
                        <Typography.Text type="secondary">暂无定义描述</Typography.Text>
                    )}
                </div>
            </div>
        </Spin>
    );
}