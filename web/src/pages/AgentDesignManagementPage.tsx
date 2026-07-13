import { Button, Form, Input, Modal, Select, Space, Table, Tooltip, Typography, Popconfirm, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { usePreventDoubleClickHook } from "../hooks/usePreventDoubleClickHook";
import { ToastUtil } from "../utils/ToastUtil";
import { AgentDefinitionApi } from "../api/agentDefinitionApi";
import { AiModelApi } from "../api/aiModelApi";
import type { AgentDefinitionMiniDto } from "../dto/agentDefinition/AgentDefinitionMiniDto";
import type { AiModelResponseDto } from "../dto/aiModel/AiModelDto";

/**
 * 获取状态中文标签。
 *
 * @param status 状态码
 * @returns 状态中文
 */
function getStatusLabel(status: string): string {
  if (status === "ENABLE") return "启用";
  if (status === "DISABLE") return "停用";
  return status || "-";
}

/**
 * 智能体设计管理页面。
 *
 * @author qty
 */
export function AgentDesignManagementPage() {
  const [dataSource, setDataSource] = useState<AgentDefinitionMiniDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);

  // 搜索条件
  const [keyword, setKeyword] = useState("");
  const [pageIndex, setPageIndex] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  // 弹窗状态
  const [showModal, setShowModal] = useState(false);
  const [form] = Form.useForm();

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

  // 加载列表
  const loadDataRef = useRef<(() => Promise<void>) | null>(null);
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await AgentDefinitionApi.listAll();
      const list = (result.records || []).filter((a: AgentDefinitionMiniDto) => a.name);
      setDataSource(list);
      setTotal(result.total || 0);
    } finally {
      setLoading(false);
    }
  }, []);

  loadDataRef.current = loadData;

  useEffect(() => {
    loadData();
    loadModels();
  }, [loadData, loadModels]);

  // 打开创建弹窗
  const openCreateModal = useCallback(() => {
    form.resetFields();
    setShowModal(true);
  }, [form]);

  // 提交
  const { onClick: handleSubmit, loading: submitLoading } = usePreventDoubleClickHook(async () => {
    const values = await form.validateFields();
    // 当前为骨架阶段，保留真实接口调用位
    ToastUtil.success("骨架提交验证成功，后续接入后端接口");
    setShowModal(false);
    loadDataRef.current?.();
  });

  // 模型名称映射
  const getModelLabel = useCallback((modelId?: string) => {
    if (!modelId) return "-";
    const m = models.find(mod => mod.id === modelId);
    return m ? `${m.providerName} · ${m.modelCode}` : modelId;
  }, [models]);

  // 表格列
  const columns = useMemo<ColumnsType<AgentDefinitionMiniDto>>(() => [
    { title: "智能体名称", dataIndex: "name", width: 160 },
    {
      title: "默认模型", dataIndex: "defaultModelId", width: 180,
      render: (val: string) => {
        const label = getModelLabel(val);
        return <Tooltip title={label}>{label}</Tooltip>;
      }
    },
    {
      title: "状态", dataIndex: "status", width: 80,
      render: (status: string) => <Tag color={status === "ENABLE" ? "green" : "red"}>{getStatusLabel(status)}</Tag>
    },
    { title: "备注", dataIndex: "remark", ellipsis: true, render: (value: string) => <Tooltip title={value}>{value || "-"}</Tooltip> },
    {
      title: "操作", width: 150,
      render: () => (
        <Space>
          <Button type="link">详情</Button>
          <Popconfirm title="确定删除该智能体吗？" okButtonProps={{ danger: true }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ], [getModelLabel]);

  return (
    <div>
      <Typography.Title level={3}>智能体设计管理</Typography.Title>

      {/* 搜索区域 */}
      <div className="simple-search-panel">
        <Space wrap>
          <Input
            placeholder="智能体名称/描述"
            value={keyword}
            onChange={e => setKeyword(e.target.value)}
            style={{ width: 200, height: 36 }}
            allowClear
          />
          <Select
            placeholder="状态"
            style={{ width: 120, height: 36 }}
            allowClear
            options={[{ label: "启用", value: "ENABLE" }, { label: "停用", value: "DISABLE" }]}
          />
          <Button type="primary" onClick={() => loadDataRef.current?.()}>搜索</Button>
          <Button onClick={() => { setKeyword(""); loadDataRef.current?.(); }}>重置</Button>
        </Space>
      </div>

      {/* 操作按钮区 */}
      <div className="simple-toolbar">
        <Space />
        <Button type="primary" onClick={openCreateModal}>新增智能体</Button>
      </div>

      {/* 数据表格 */}
      <Table
        rowKey="id"
        bordered
        columns={columns}
        dataSource={dataSource}
        loading={loading}
        pagination={{
          current: pageIndex,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: t => `共 ${t} 条`,
          onChange: (p, s) => { setPageIndex(p); setPageSize(s); }
        }}
      />

      {/* 创建弹窗 */}
      <Modal
        open={showModal}
        onCancel={() => setShowModal(false)}
        title="新增智能体"
        width={520}
        centered
        footer={[
          <Button key="cancel" onClick={() => setShowModal(false)}>取消</Button>,
          <Button key="confirm" type="primary" loading={submitLoading} onClick={handleSubmit}>确认创建</Button>
        ]}
      >
        <div className="simple-form-container">
          <Form form={form} layout="horizontal" size="middle" labelCol={{ span: 5 }} wrapperCol={{ span: 19 }}>
            <Form.Item
              label="名称"
              name="name"
              rules={[{ required: true, message: "请输入名称" }]}
              style={{ marginBottom: 16 }}
            >
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item
              label="默认模型"
              name="defaultModelId"
              rules={[{ required: true, message: "请选择默认模型" }]}
              style={{ marginBottom: 16 }}
            >
              <Select
                placeholder="选择默认模型"
                style={{ height: 36 }}
                options={models.filter(m => m.status === 1).map(m => ({
                  label: `${m.providerName} · ${m.modelCode}`,
                  value: m.id
                }))}
                showSearch
                filterOption={(input, option) => (option?.label as string || "").includes(input)}
              />
            </Form.Item>
            <Form.Item label="状态" name="status" style={{ marginBottom: 16 }}>
              <Select
                style={{ height: 36 }}
                options={[{ label: "启用", value: "ENABLE" }, { label: "停用", value: "DISABLE" }]}
              />
            </Form.Item>
            <Form.Item
              label="描述"
              name="definitionDesc"
              rules={[{ required: true, message: "请输入描述" }]}
              style={{ marginBottom: 16 }}
            >
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item label="备注" name="remark" style={{ marginBottom: 0 }}>
              <Input.TextArea rows={3} />
            </Form.Item>
          </Form>
        </div>
      </Modal>
    </div>
  );
}
