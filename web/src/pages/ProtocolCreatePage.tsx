import {Button, Form, Input, Select, Space, Typography} from "antd";
import {ArrowLeftOutlined} from "@ant-design/icons";
import {useState} from "react";
import {useNavigate} from "react-router-dom";
import MDEditor from "@uiw/react-md-editor";
import {usePreventDoubleClickHook} from "../hooks/usePreventDoubleClickHook";
import {ToastUtil} from "../utils/ToastUtil";
import {ProtocolApi} from "../api/protocolApi";
import type {CreateProtocolRequestDto} from "../dto/protocol/ProtocolDto";

/**
 * 执行器协议新增页面。
 * 上方表单区域填写元信息，下方 MD 编辑器编写协议内容，支持分屏实时预览。
 *
 * @author qty
 */
export function ProtocolCreatePage() {
    const navigate = useNavigate();
    const [form] = Form.useForm();

    // MD 编辑器内容
    const [mdContent, setMdContent] = useState("");

    // 提交创建
    const {onClick: handleSubmit, loading: submitLoading} = usePreventDoubleClickHook(async () => {
        const values = await form.validateFields();

        // 校验 MD 内容不为空
        if (!mdContent.trim()) {
            ToastUtil.error("请输入协议内容");
            return;
        }

        const createData: CreateProtocolRequestDto = {
            protocolCode: values.protocolCode,
            protocolName: values.protocolName,
            protocolVersion: values.protocolVersion,
            content: mdContent
        };
        await ProtocolApi.create(createData);
        ToastUtil.success("创建成功");
        navigate("/agent-protocol");
    });

    return (
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
                    <Typography.Text type="secondary">协议管理 &gt; 新增协议</Typography.Text>
                </Space>
            </div>

            <Typography.Title level={3}>新增协议</Typography.Title>

            {/* 元信息表单 */}
            <div className="simple-search-panel">
                <Form form={form} layout="inline" size="middle">
                    <Form.Item
                        label="协议名称"
                        name="protocolName"
                        rules={[{required: true, message: "请输入协议名称"}]}
                        style={{marginBottom: 16}}
                    >
                        <Input style={{width: 240, height: 36}} placeholder="如 Simple Executor Protocol v1.0"/>
                    </Form.Item>
                    <Form.Item
                        label="协议编码"
                        name="protocolCode"
                        rules={[{required: true, message: "请输入协议编码"}]}
                        style={{marginBottom: 16}}
                    >
                        <Input style={{width: 200, height: 36}} placeholder="如 SEP_V1"/>
                    </Form.Item>
                    <Form.Item
                        label="协议版本"
                        name="protocolVersion"
                        rules={[{required: true, message: "请输入协议版本"}]}
                        style={{marginBottom: 16}}
                    >
                        <Input style={{width: 120, height: 36}} placeholder="如 v1.0"/>
                    </Form.Item>
                    <Form.Item label="状态" name="status" style={{marginBottom: 16}}>
                        <Select
                            placeholder="请选择状态"
                            style={{width: 120, height: 36}}
                            options={[
                                {label: "启用", value: "ON"},
                                {label: "停用", value: "OFF"}
                            ]}
                        />
                    </Form.Item>
                </Form>
            </div>

            {/* MD 编辑器区域 */}
            <Typography.Title level={5} style={{marginTop: 16, marginBottom: 8}}>协议内容（Markdown）</Typography.Title>
            <div data-color-mode="light">
                <MDEditor
                    value={mdContent}
                    onChange={val => setMdContent(val || "")}
                    height={520}
                    preview="live"
                />
            </div>

            {/* 底部操作按钮 */}
            <div style={{marginTop: 16, textAlign: "right"}}>
                <Space>
                    <Button onClick={() => navigate("/agent-protocol")}>取消</Button>
                    <Button type="primary" loading={submitLoading} onClick={handleSubmit}>保存</Button>
                </Space>
            </div>
        </div>
    );
}