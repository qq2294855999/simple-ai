import {Button, Form, Input, Select, Space, Spin, Typography} from "antd";
import {ArrowLeftOutlined} from "@ant-design/icons";
import {useMemo, useState} from "react";
import {useNavigate} from "react-router-dom";
import MDEditor from "@uiw/react-md-editor";
import {usePreventDoubleClickHook} from "../hooks/usePreventDoubleClickHook";
import {useScrollSelect} from "../hooks/useScrollSelect";
import {ToastUtil} from "../utils/ToastUtil";
import {AgentSkillApi} from "../api/agentSkillApi";
import {AgentDefinitionApi} from "../api/agentDefinitionApi";
import type {CreateAgentSkillRequestDto} from "../dto/agentSkill/CreateAgentSkillRequestDto";

/**
 * 智能体技能新增页面。
 * 定义描述和执行内容使用 MD 编辑器，支持分屏实时预览。
 *
 * @author qty
 */
export function AgentSkillCreatePage() {
    const navigate = useNavigate();
    const [form] = Form.useForm();

    // 定义描述 MD 内容
    const [definitionDesc, setDefinitionDesc] = useState("");

    // 执行内容 MD 内容
    const [execContent, setExecContent] = useState("");

    // 智能体下拉：滚动分页加载
    const {
        options: agentsRaw,
        loading: agentsLoading,
        onPopupScroll: onAgentsPopupScroll
    } = useScrollSelect<{ id: string; name?: string }>(
        (page, size) => AgentDefinitionApi.page({current: page, size}),
        20,
        []
    );

    const agents = useMemo(
        () => agentsRaw.filter(a => a.name).map(a => ({id: a.id, name: a.name!})),
        [agentsRaw]
    );

    // 提交创建
    const {onClick: handleSubmit, loading: submitLoading} = usePreventDoubleClickHook(async () => {
        const values = await form.validateFields();

        // 校验 MD 内容非空
        if (!definitionDesc.trim()) {
            ToastUtil.warning("请输入定义描述");
            return;
        }
        if (!execContent.trim()) {
            ToastUtil.warning("请输入执行内容");
            return;
        }

        const createData: CreateAgentSkillRequestDto = {
            agentId: values.agentId,
            definitionDesc,
            execContent,
            returnDataFormat: values.returnDataFormat,
            remark: values.remark
        };
        await AgentSkillApi.create(createData);
        ToastUtil.success("创建成功");
        navigate("/agent-skill");
    });

    return (
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
                    <Typography.Text type="secondary">技能管理 &gt; 新增技能</Typography.Text>
                </Space>
            </div>

            <Typography.Title level={3}>新增技能</Typography.Title>

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
                            onPopupScroll={onAgentsPopupScroll}
                            notFoundContent={agentsLoading ? <Spin size="small"/> : "暂无数据"}
                            filterOption={(input, option) => (option?.label as string || "").includes(input)}
                        />
                    </Form.Item>
                    <Form.Item
                        label="返回格式"
                        name="returnDataFormat"
                        style={{marginBottom: 16}}
                    >
                        <Input style={{width: 200, height: 36}} placeholder="如 JSON"/>
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
                    height={320}
                    preview="live"
                />
            </div>

            {/* 执行内容 MD 编辑器 */}
            <Typography.Title level={5} style={{marginTop: 16, marginBottom: 8}}>执行内容（Markdown）</Typography.Title>
            <div data-color-mode="light">
                <MDEditor
                    value={execContent}
                    onChange={val => setExecContent(val || "")}
                    height={320}
                    preview="live"
                />
            </div>

            {/* 底部操作按钮 */}
            <div style={{marginTop: 16, textAlign: "right"}}>
                <Space>
                    <Button onClick={() => navigate("/agent-skill")}>取消</Button>
                    <Button type="primary" loading={submitLoading} onClick={handleSubmit}>保存</Button>
                </Space>
            </div>
        </div>
    );
}