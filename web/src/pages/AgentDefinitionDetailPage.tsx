import {Button, Descriptions, Space, Spin, Tag, Typography} from "antd";
import {ArrowLeftOutlined, EditOutlined} from "@ant-design/icons";
import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import {ToastUtil} from "../utils/ToastUtil";
import {AgentDefinitionApi} from "../api/agentDefinitionApi";
import {AiModelApi} from "../api/aiModelApi";
import {RestrictedMarkdownComponent} from "../components/agentChat/RestrictedMarkdownComponent";
import type {AgentDefinitionInfoDto} from "../dto/agentDefinition/AgentDefinitionDto";
import type {AiModelResponseDto} from "../dto/aiModel/AiModelDto";

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
 * 智能体定义详情只读页面。
 * 定义描述以 Markdown 渲染展示。
 *
 * @author qty
 */
export function AgentDefinitionDetailPage() {
    const navigate = useNavigate();
    const {id} = useParams<{ id: string }>();

    const [record, setRecord] = useState<AgentDefinitionInfoDto | null>(null);
    const [pageLoading, setPageLoading] = useState(false);
    const [models, setModels] = useState<AiModelResponseDto[]>([]);

    // 获取模型名称
    const getModelLabel = (modelId?: string): string => {
        if (!modelId) return "-";
        const m = models.find(mod => mod.id === modelId);
        return m ? `${m.providerName} · ${m.modelCode}` : modelId;
    };

    // 加载详情数据和模型列表
    useEffect(() => {
        if (!id) return;
        setPageLoading(true);
        Promise.all([AgentDefinitionApi.findOne(id), AiModelApi.list()])
            .then(([detail, modelList]) => {
                setRecord(detail);
                setModels(modelList || []);
            })
            .catch(() => {
                ToastUtil.error("获取智能体详情失败");
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
                            onClick={() => navigate("/agent-design")}
                        >
                            返回列表
                        </Button>
                        <Typography.Text type="secondary">智能体设计 &gt; 智能体详情</Typography.Text>
                    </Space>
                </div>

                <div style={{display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16}}>
                    <Typography.Title level={3} style={{margin: 0}}>智能体详情</Typography.Title>
                    <Button
                        type="primary"
                        icon={<EditOutlined/>}
                        onClick={() => navigate(`/agent-design/${id}/edit`)}
                    >
                        编辑
                    </Button>
                </div>

                {/* 元信息 */}
                {record && (
                    <div className="simple-search-panel">
                        <Descriptions column={3} bordered size="small">
                            <Descriptions.Item label="名称">{record.name}</Descriptions.Item>
                            <Descriptions.Item label="默认模型">{getModelLabel(record.defaultModelId)}</Descriptions.Item>
                            <Descriptions.Item label="状态">
                                <Tag color={getStatusLabel(record.status ?? 0) === "启用" ? "green" : "red"}>
                                    {getStatusLabel(record.status ?? 0)}
                                </Tag>
                            </Descriptions.Item>
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
                        minHeight: 300,
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