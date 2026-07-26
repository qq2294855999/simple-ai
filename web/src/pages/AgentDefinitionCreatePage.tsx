import {Button, Form, Input, Select, Space, Typography} from "antd";
import {ArrowLeftOutlined} from "@ant-design/icons";
import {useCallback, useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import MDEditor from "@uiw/react-md-editor";
import {usePreventDoubleClickHook} from "../hooks/usePreventDoubleClickHook";
import {ToastUtil} from "../utils/ToastUtil";
import {AgentDefinitionApi} from "../api/agentDefinitionApi";
import {AiModelApi} from "../api/aiModelApi";
import type {CreateAgentDefinitionDto} from "../dto/agentDefinition/AgentDefinitionDto";
import type {AiModelResponseDto} from "../dto/aiModel/AiModelDto";

/**
 * 智能体定义新增页面。
 * 定义描述使用 MD 编辑器，支持分屏实时预览。
 *
 * @author qty
 */
export function AgentDefinitionCreatePage() {
    const navigate = useNavigate();
    const [form] = Form.useForm();

    // 定义描述 MD 内容
    const [definitionDesc, setDefinitionDesc] = useState("");

    // 模型下拉数据
    const [models, setModels] = useState<AiModelResponseDto[]>([]);

    // 加载模型下拉
    const loadModels = useCallback(async () => {
        try {
            const result = await AiModelApi.list();
            setModels(result || []);
        } catch {
            // 下拉加载失败不影响主流程
        }
    }, []);

    useEffect(() => {
        loadModels();
    }, [loadModels]);

    // 提交创建
    const {onClick: handleSubmit, loading: submitLoading} = usePreventDoubleClickHook(async () => {
        const values = await form.validateFields();

        // 校验 MD 内容非空
        if (!definitionDesc.trim()) {
            ToastUtil.warning("请输入定义描述");
            return;
        }

        const createData: CreateAgentDefinitionDto = {
            name: values.name,
            definitionDesc,
            defaultModelId: values.defaultModelId,
            remark: values.remark
        };
        await AgentDefinitionApi.create(createData);
        ToastUtil.success("创建成功");
        navigate("/agent-design");
    });

    return (
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
                    <Typography.Text type="secondary">智能体设计 &gt; 新增智能体</Typography.Text>
                </Space>
            </div>

            <Typography.Title level={3}>新增智能体</Typography.Title>

            {/* 元信息表单 */}
            <div className="simple-search-panel">
                <Form form={form} layout="inline" size="middle">
                    <Form.Item
                        label="名称"
                        name="name"
                        rules={[{required: true, message: "请输入名称"}]}
                        style={{marginBottom: 16}}
                    >
                        <Input style={{width: 200, height: 36}} placeholder="智能体名称"/>
                    </Form.Item>
                    <Form.Item
                        label="默认模型"
                        name="defaultModelId"
                        rules={[{required: true, message: "请选择默认模型"}]}
                        style={{marginBottom: 16}}
                    >
                        <Select
                            placeholder="选择默认模型"
                            style={{width: 280, height: 36}}
                            options={models.filter(m => m.status === 1).map(m => ({
                                label: `${m.providerName} · ${m.modelCode}`,
                                value: m.id
                            }))}
                            showSearch
                            filterOption={(input, option) => (option?.label as string || "").includes(input)}
                        />
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

            {/* 定义描述 MD 编辑器 */}
            <Typography.Title level={5} style={{marginTop: 16, marginBottom: 8}}>定义描述（Markdown）</Typography.Title>
            <div data-color-mode="light">
                <MDEditor
                    value={definitionDesc}
                    onChange={val => setDefinitionDesc(val || "")}
                    height={520}
                    preview="live"
                />
            </div>

            {/* 底部操作按钮 */}
            <div style={{marginTop: 16, textAlign: "right"}}>
                <Space>
                    <Button onClick={() => navigate("/agent-design")}>取消</Button>
                    <Button type="primary" loading={submitLoading} onClick={handleSubmit}>保存</Button>
                </Space>
            </div>
        </div>
    );
}