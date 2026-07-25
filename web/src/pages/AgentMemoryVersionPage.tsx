import {Button, Card, Modal, Space, Tag, Timeline, Typography} from "antd";
import {useCallback, useEffect, useState} from "react";
import {useSearchParams} from "react-router-dom";
import {AgentMemoryApi} from "../api/agentMemoryApi";
import type {AgentMemoryInfoResponseDto, MemoryVersionHistoryResponseDto} from "../dto/agentMemory/AgentMemoryDto";

/**
 * 获取版本状态中文标签。
 *
 * @param status 版本状态码
 * @returns 状态中文
 */
function getVersionStatusLabel(status: number): string {
    if (status === 1) return "草稿";
    if (status === 2) return "已发布";
    if (status === 3) return "已退役";
    return String(status);
}

/**
 * 获取版本状态Tag颜色。
 *
 * @param status 版本状态码
 * @returns Tag颜色
 */
function getVersionStatusColor(status: number): string {
    if (status === 1) return "blue";
    if (status === 2) return "green";
    if (status === 3) return "default";
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
 * 记忆版本历史页面。
 *
 * <p>展示某记忆的版本演进链路，以时间线形式呈现。</p>
 *
 * @author qty
 */
export function AgentMemoryVersionPage() {
    const [searchParams] = useSearchParams();
    const memoryId = searchParams.get("memoryId") || "";

    const [history, setHistory] = useState<MemoryVersionHistoryResponseDto[]>([]);
    const [loading, setLoading] = useState(false);

    // 详情弹窗
    const [showDetailModal, setShowDetailModal] = useState(false);
    const [detailData, setDetailData] = useState<AgentMemoryInfoResponseDto | null>(null);

    // 加载版本历史
    const loadHistory = useCallback(async () => {
        if (!memoryId) return;
        setLoading(true);
        try {
            const result = await AgentMemoryApi.versionHistory(memoryId);
            setHistory(result || []);
        } finally {
            setLoading(false);
        }
    }, [memoryId]);

    useEffect(() => {
        loadHistory();
    }, [loadHistory]);

    // 查看某版本详情
    const handleViewDetail = useCallback(async (id: string) => {
        try {
            const record = await AgentMemoryApi.findOne(id);
            setDetailData(record);
            setShowDetailModal(true);
        } catch {
            // 获取详情失败不阻塞
        }
    }, []);

    return (
        <div>
            <Typography.Title level={3}>记忆版本历史</Typography.Title>

            <Card loading={loading}>
                {history.length === 0 && !loading ? (
                    <div style={{textAlign: "center", padding: 40, color: "#999"}}>
                        {memoryId ? "暂无版本历史数据" : '请从记忆管理页面点击"版本历史"进入'}
                    </div>
                ) : (
                    <Timeline
                        items={history.map(item => ({
                            color: item.versionStatus === 2 ? "green" : item.versionStatus === 1 ? "blue" : "gray",
                            children: (
                                <div style={{display: "flex", alignItems: "center", gap: 12, padding: "8px 0"}}>
                                    <div>
                                        <div style={{fontWeight: 600, fontSize: 14}}>
                                            v{item.versionNo}
                                            <Tag color={getVersionStatusColor(item.versionStatus)} style={{marginLeft: 8}}>
                                                {getVersionStatusLabel(item.versionStatus)}
                                            </Tag>
                                            <Tag style={{marginLeft: 4}}>{getCreateReasonLabel(item.createReason)}</Tag>
                                        </div>
                                        <div style={{color: "#666", fontSize: 12, marginTop: 4}}>
                                            {item.summary || "-"}
                                        </div>
                                        <div style={{color: "#999", fontSize: 12, marginTop: 2}}>
                                            {item.createTime || "-"}
                                        </div>
                                    </div>
                                    <Button type="link" size="small" onClick={() => handleViewDetail(item.id)}>查看</Button>
                                </div>
                            )
                        }))}
                    />
                )}
            </Card>

            {/* 版本详情弹窗 */}
            <Modal
                open={showDetailModal}
                onCancel={() => setShowDetailModal(false)}
                title={detailData ? `版本详情 v${detailData.versionNo}` : "版本详情"}
                width={640}
                centered
                footer={<Button onClick={() => setShowDetailModal(false)}>关闭</Button>}
            >
                {detailData && (
                    <div style={{lineHeight: 2}}>
                        <div><b>记忆名称：</b>{detailData.memoryName}</div>
                        <div><b>版本号：</b>v{detailData.versionNo}</div>
                        <div><b>版本状态：</b><Tag
                            color={getVersionStatusColor(detailData.versionStatus)}>{getVersionStatusLabel(detailData.versionStatus)}</Tag></div>
                        <div><b>创建原因：</b>{getCreateReasonLabel(detailData.createReason)}</div>
                        {detailData.parentMemoryName && <div><b>父版本：</b>{detailData.parentMemoryName}</div>}
                        <div><b>摘要：</b>{detailData.summary || "-"}</div>
                        <div><b>参数定义：</b>
                            <pre style={{fontSize: 12, background: "#f5f5f5", padding: 8, borderRadius: 4, maxHeight: 120, overflow: "auto"}}>
                {detailData.paramsDefinition || "{}"}
              </pre>
                        </div>
                        <div><b>步骤列表：</b></div>
                        <div style={{fontSize: 12}}>
                            {(detailData.steps || []).map((step, idx) => (
                                <div key={step.id || idx} style={{padding: "4px 0", borderBottom: "1px solid #f0f0f0"}}>
                                    <Space>
                                        <Tag color="blue">{step.sequenceNo}</Tag>
                                        <span>{step.stepName}</span>
                                        <Tag>{step.atomicCommandCode || "-"}</Tag>
                                    </Space>
                                </div>
                            ))}
                        </div>
                        <div><b>备注：</b>{detailData.remark || "-"}</div>
                    </div>
                )}
            </Modal>
        </div>
    );
}