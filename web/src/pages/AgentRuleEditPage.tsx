import {Button, Form, Input, Select, Space, Spin, Typography} from "antd";
import {ArrowLeftOutlined} from "@ant-design/icons";
import {useCallback, useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import MDEditor from "@uiw/react-md-editor";
import {usePreventDoubleClickHook} from "../hooks/usePreventDoubleClickHook";
import {ToastUtil} from "../utils/ToastUtil";
import {AgentRuleApi} from "../api/agentRuleApi";
import {AgentDefinitionApi} from "../api/agentDefinitionApi";
import type {UpdateAgentRuleRequestDto} from "../dto/agentRule/AgentRuleDto";

/**
 * 智能体规则编辑页面。
 * 回显已有数据，触发条件和定义描述使用 MD 编辑器。
 *
 * @author qty
 */
export function AgentRuleEditPage() {
    const navigate = useNavigate();
    const {id} = useParams<{ id: string }>();
    const [form] = Form.useForm();

    // 触发条件 MD 内容
    const [triggerCondition, setTriggerCondition] = useState("");

    // 定义描述 MD 内容
    const [definitionDesc, setDefinitionDesc] = useState("");

    // 页面加载状态
    const [pageLoading, setPageLoading] = useState(false);

    // 智能体下拉数据
    const [agents, setAgents] = useState<{ id: string; name: string }[]>([]);

    // 加载智能体下拉
    const loadAgents = useCallback(async () => {
        try {
            const result = await AgentDefinitionApi.listAll();
            const list = (result.records || [])
                .filter((a: { name?: string }) => a.name)
                .map((a: { id: string; name: string }) => ({id: a.id, name: a.name}));
            setAgents(list);
        } catch {
            // 下拉加载失败不影响主流程
        }
    }, []);

    // 加载已有数据回显
    useEffect(() => {
        if (!id) return;
        setPageLoading(true);
        Promise.all([AgentRuleApi.findOne(id), loadAgents()])
            .then(([record]) => {
                form.setFieldsValue({
                    agentId: record.agentId,
                    triggerAction: record.triggerAction,
                    remark: record.remark
                });
                setTriggerCondition(record.triggerCondition || "");
                setDefinitionDesc(record.definitionDesc || "");
            })
            .catch(() => {
                ToastUtil.error("获取规则详情失败");
            })
            .finally(() => {
                setPageLoading(false);
            });
    }, [id, form, loadAgents]);

    // 提交更新
    const {onClick: handleSubmit, loading: submitLoading} = usePreventDoubleClickHook(async () => {
        if (!id) return;
        const values = await form.validateFields();

        // 校验 MD 内容非空
        if (!triggerCondition.trim()) {
            ToastUtil.warning("请输入触发条件");
            return;
        }
        if (!definitionDesc.trim()) {
            ToastUtil.warning("请输入定义描述");
            return;
        }

        const updateData: UpdateAgentRuleRequestDto = {
            id,
            agentId: values.agentId,
            definitionDesc,
            triggerCondition,
            triggerAction: values.triggerAction,
            remark: values.remark
        };
        await AgentRuleApi.update(id, updateData);
        ToastUtil.success("更新成功");
        navigate("/agent-rule");
    });

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
                        <Typography.Text type="secondary">规则管理 &gt; 编辑规则</Typography.Text>
                    </Space>
                </div>

                <Typography.Title level={3}>编辑规则</Typography.Title>

                {/* 元信息表单 */}
                <div className="simple-search-panel">
                    <Form form={form} layout="inline" size="middle">
                        <Form.Item
                            label="所属智能体"
                            name="agentId"
                            rules={[{required: true, message: "请选择智能体"}]}
                            style={{marginBottom: 16}}
                        >
                            <Select
                                placeholder="选择智能体"
                                style={{width: 200, height: 36}}
                                options={agents.map(a => ({label: a.name, value: a.id}))}
                                showSearch
                                filterOption={(input, option) => (option?.label as string || "").includes(input)}
                            />
                        </Form.Item>
                        <Form.Item
                            label="触发动作"
                            name="triggerAction"
                            style={{marginBottom: 16}}
                        >
                            <Input style={{width: 240, height: 36}} placeholder="触发动作摘要"/>
                        </Form.Item>
                        <Form.Item
                            label="备注"
                            name="remark"
                            style={{marginBottom: 16}}
                        >
                            <Input style={{width: 200, height: 36}} placeholder="可选"/>
                        </Form.Item>
                    </Form>
                </div>

                {/* 触发条件 MD 编辑器 */}
                <Typography.Title level={5} style={{marginTop: 16, marginBottom: 8}}>触发条件（Markdown）</Typography.Title>
                <div data-color-mode="light">
                    <MDEditor
                        value={triggerCondition}
                        onChange={val => setTriggerCondition(val || "")}
                        height={320}
                        preview="live"
                    />
                </div>

                {/* 定义描述 MD 编辑器 */}
                <Typography.Title level={5} style={{marginTop: 16, marginBottom: 8}}>定义描述（Markdown）</Typography.Title>
                <div data-color-mode="light">
                    <MDEditor
                        value={definitionDesc}
                        onChange={val => setDefinitionDesc(val || "")}
                        height={320}
                        preview="live"
                    />
                </div>

                {/* 底部操作按钮 */}
                <div style={{marginTop: 16, textAlign: "right"}}>
                    <Space>
                        <Button onClick={() => navigate("/agent-rule")}>取消</Button>
                        <Button type="primary" loading={submitLoading} onClick={handleSubmit}>保存</Button>
                    </Space>
                </div>
            </div>
        </Spin>
    );
}