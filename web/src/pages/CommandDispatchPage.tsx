import { Button, Card, Form, Input, Select, Space, Table, Tag, Tooltip, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useCallback, useEffect, useState } from "react";
import { AgentDefinitionApi } from "../api/agentDefinitionApi";
import { AgentSkillApi } from "../api/agentSkillApi";
import { AiModelApi } from "../api/aiModelApi";
import { CommandDispatchApi } from "../api/commandDispatchApi";
import type { AgentSkillPageResponseDto } from "../dto/agentSkill/AgentSkillPageResponseDto";
import type { AiModelResponseDto } from "../dto/aiModel/AiModelDto";
import type { CommandDispatchProgressEventDto, CommandDispatchRequestDto } from "../dto/command/CommandDispatchDto";
import type { AgentDefinitionMiniDto } from "../dto/agentDefinition/AgentDefinitionMiniDto";
import { usePreventDoubleClickHook } from "../hooks/usePreventDoubleClickHook";
import { ToastUtil } from "../utils/ToastUtil";

interface CommandDispatchFormDto {
  agentId: string;
  skillId?: string;
  modelId?: string;
  commandName: string;
  commandContent: string;
  sessionId?: string;
}

const finalEventTypes = new Set(["TASK_COMPLETED", "TASK_FAILED"]);
const maxProgressEventCount = 500;

/**
 * 命令调度页面。
 *
 * @author qty
 */
export function CommandDispatchPage() {
  const [form] = Form.useForm<CommandDispatchFormDto>();
  const selectedAgentId = Form.useWatch("agentId", form);
  const [agents, setAgents] = useState<AgentDefinitionMiniDto[]>([]);
  const [models, setModels] = useState<AiModelResponseDto[]>([]);
  const [skills, setSkills] = useState<AgentSkillPageResponseDto[]>([]);
  const [events, setEvents] = useState<CommandDispatchProgressEventDto[]>([]);
  const [resultContent, setResultContent] = useState("");

  // 运行时模型快照
  const [runtimeSnapshot, setRuntimeSnapshot] = useState("");

  const loadAgents = useCallback(async () => {
    const page = await AgentDefinitionApi.listAll();

    // 展示后端聚合列表，最终可执行性由调度服务统一校验
    setAgents(page.records);
  }, []);

  const loadModels = useCallback(async (agentId: string) => {
    const result = await AiModelApi.available(agentId);
    setModels(result);
  }, []);

  const loadSkills = useCallback(async (agentId: string) => {
    const page = await AgentSkillApi.page({ current: 1, size: 1000, agentId });

    // 技能仅用于辅助用户理解当前智能体能力，不参与现有调度请求参数
    setSkills(page.records);
  }, []);

  useEffect(() => {
    void loadAgents().catch(() => setAgents([]));
  }, [loadAgents]);

  useEffect(() => {
    form.setFieldValue("skillId", undefined);
    form.setFieldValue("modelId", undefined);
    setSkills([]);
    setModels([]);

    // 选定智能体后仅加载其启用技能和可用模型，防止跨智能体误选
    if (selectedAgentId) {
      void loadSkills(selectedAgentId).catch(() => setSkills([]));
      void loadModels(selectedAgentId).catch(() => setModels([]));
    }
  }, [form, loadSkills, loadModels, selectedAgentId]);

  const handleProgress = useCallback((event: CommandDispatchProgressEventDto) => {

    // 仅保留近期进度事件，防止长时间流式任务持续占用页面内存
    setEvents(previousEvents => [...previousEvents, event].slice(-maxProgressEventCount));

    // 捕获首次可用的运行时模型快照
    if (!runtimeSnapshot && event.providerName && event.modelCode) {
      setRuntimeSnapshot(`${event.providerName} · ${event.modelCode}`);
    }

    // AI token 按顺序累积，供用户实时查看生成内容
    if (event.eventType === "AI_TOKEN") {
      setResultContent(previousContent => previousContent + event.payload);
      return;
    }

    // 最终事件以完整响应覆盖临时 token 内容
    if (event.eventType === "TASK_COMPLETED") {
      setResultContent(event.payload);
    }
  }, [runtimeSnapshot]);

  const { onClick: handleDispatch, loading } = usePreventDoubleClickHook(async () => {
    const values = await form.validateFields();
    const request: CommandDispatchRequestDto = {
      agentId: values.agentId,
      commandName: values.commandName,
      commandContent: values.commandContent,
      sessionId: values.sessionId
    };

    // 用户显式选择模型时传递 modelId
    if (values.modelId) {
      request.modelId = values.modelId;
    }

    // 新任务开始前清空上一次调度过程和结果
    setEvents([]);
    setResultContent("");
    setRuntimeSnapshot("");
    await CommandDispatchApi.dispatchStream(request, handleProgress);
    ToastUtil.success("命令调度已完成");
  });

  const eventColumns: ColumnsType<CommandDispatchProgressEventDto> = [
    { title: "事件", dataIndex: "eventType", align: "center", width: 170, render: value => <Tag color={finalEventTypes.has(value) ? "green" : "blue"}>{value}</Tag> },
    { title: "进度说明", dataIndex: "message", align: "center", ellipsis: true, render: value => <Tooltip title={value}>{value}</Tooltip> },
    { title: "执行状态", dataIndex: "execStatus", align: "center", width: 120, render: value => <Tooltip title={value}>{value || "-"}</Tooltip> },
    { title: "失败原因", dataIndex: "failureReason", align: "center", ellipsis: true, render: value => <Tooltip title={value}>{value || "-"}</Tooltip> }
  ];

  return (
    <div>
      <Typography.Title level={3}>命令调度</Typography.Title>
      <div className="simple-search-panel">
        <Form form={form} layout="inline">
          <Form.Item name="agentId" rules={[{ required: true, message: "请选择智能体" }]}>
            <Select placeholder="选择智能体" style={{ width: 200, height: 36 }} options={agents.map(agent => ({ label: agent.name, value: agent.id }))} />
          </Form.Item>
          <Form.Item name="modelId">
            <Select placeholder="选择模型（可选）" allowClear disabled={!selectedAgentId || models.length === 0} style={{ width: 240, height: 36 }}
              options={models.map(model => ({ label: `${model.providerName} · ${model.modelName}`, value: model.id }))} />
          </Form.Item>
          <Form.Item name="skillId">
            <Select placeholder="参考技能（可选）" disabled={!selectedAgentId} style={{ width: 200, height: 36 }} options={skills.map(skill => ({ label: skill.definitionDesc, value: skill.id }))} />
          </Form.Item>
          <Form.Item name="commandName" rules={[{ required: true, message: "请输入命令名称" }]}>
            <Input placeholder="命令名称" style={{ width: 160, height: 36 }} />
          </Form.Item>
          <Form.Item name="commandContent" rules={[{ required: true, message: "请输入命令内容" }]}>
            <Input placeholder="命令内容" style={{ width: 240, height: 36 }} />
          </Form.Item>
          <Form.Item name="sessionId">
            <Input placeholder="会话 ID（可选）" style={{ width: 180, height: 36 }} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" loading={loading} onClick={handleDispatch}>提交调度</Button>
          </Form.Item>
        </Form>
      </div>
      <div className="simple-toolbar">
        <Space>
          <Typography.Text>调度进度</Typography.Text>
          {runtimeSnapshot && <Tooltip title="本次调用实际使用的供应商·模型"><Tag color="geekblue">{runtimeSnapshot}</Tag></Tooltip>}
        </Space>
        <Button onClick={() => void loadAgents()}>刷新智能体</Button>
      </div>
      <Table rowKey={(record, index) => `${record.taskId}-${record.eventType}-${index}`} bordered columns={eventColumns} dataSource={events} pagination={false} />
      <Card title={runtimeSnapshot ? `最终响应（${runtimeSnapshot}）` : "最终响应"} style={{ marginTop: 16 }}>
        <Typography.Paragraph style={{ whiteSpace: "pre-wrap", marginBottom: 0 }}>{resultContent || "等待提交调度任务"}</Typography.Paragraph>
      </Card>
    </div>
  );
}
