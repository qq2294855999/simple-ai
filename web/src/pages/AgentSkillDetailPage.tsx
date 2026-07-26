import {Button, Descriptions, Space, Spin, Tag, Typography} from "antd";
import {ArrowLeftOutlined, EditOutlined} from "@ant-design/icons";
import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import {ToastUtil} from "../utils/ToastUtil";
import {AgentSkillApi} from "../api/agentSkillApi";
import {RestrictedMarkdownComponent} from "../components/agentChat/RestrictedMarkdownComponent";
import type {AgentSkillInfoResponseDto} from "../dto/agentSkill/CreateAgentSkillRequestDto";

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
 * 智能体技能详情只读页面。
 * 定义描述和执行内容以 Markdown 渲染展示。
 *
 * @author qty
 */
export function AgentSkillDetailPage() {
    const navigate = useNavigate();
    const {id} = useParams<{ id: string }>();

    const [record, setRecord] = useState<AgentSkillInfoResponseDto | null>(null);
    const [pageLoading, setPageLoading] = useState(false);

    // 加载详情数据
    useEffect(() => {
        if (!id) return;
        setPageLoading(true);
        AgentSkillApi.findOne(id)
            .then(setRecord)
            .catch(() => {
                ToastUtil.error("获取技能详情失败");
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
                            onClick={() => navigate("/agent-skill")}
                        >
                            返回列表
                        </Button>
                        <Typography.Text type="secondary">技能管理 &gt; 技能详情</Typography.Text>
                    </Space>
                </div>

                <div style={{display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16}}>
                    <Typography.Title level={3} style={{margin: 0}}>技能详情</Typography.Title>
                    <Button
                        type="primary"
                        icon={<EditOutlined/>}
                        onClick={() => navigate(`/agent-skill/${id}/edit`)}
                    >
                        编辑
                    </Button>
                </div>

                {/* 元信息 */}
                {record && (
                    <div className="simple-search-panel">
                        <Descriptions column={3} bordered size="small">
                            <Descriptions.Item label="返回格式">{record.returnDataFormat}</Descriptions.Item>
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

                {/* 执行内容 Markdown 渲染区 */}
                <Typography.Title level={5} style={{marginTop: 24, marginBottom: 8}}>执行内容</Typography.Title>
                <div
                    className="simple-search-panel"
                    style={{
                        padding: 24,
                        minHeight: 200,
                        background: "#fafafa"
                    }}
                >
                    {record?.execContent ? (
                        <RestrictedMarkdownComponent content={record.execContent}/>
                    ) : (
                        <Typography.Text type="secondary">暂无执行内容</Typography.Text>
                    )}
                </div>
            </div>
        </Spin>
    );
}