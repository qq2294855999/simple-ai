import { Button, Form, Input, InputNumber, Modal, Select, Space, Table, Tooltip, Typography, Popconfirm, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { usePreventDoubleClickHook } from "../hooks/usePreventDoubleClickHook";
import { ToastUtil } from "../utils/ToastUtil";
import { AiModelProviderApi } from "../api/aiModelProviderApi";
import type { AiModelProviderResponseDto, AiModelProviderSaveRequestDto } from "../dto/aiModelProvider/AiModelProviderDto";

/**
 * 获取状态中文标签。
 *
 * @param status 状态码
 * @returns 状态中文
 */
function getStatusLabel(status: number): string {
  if (status === 1) return "启用";
  if (status === 0) return "停用";
  return String(status);
}

/**
 * AI 模型供应商管理页面。
 *
 * @author qty
 */
export function AiModelProviderManagementPage() {
  const [dataSource, setDataSource] = useState<AiModelProviderResponseDto[]>([]);
  const [loading, setLoading] = useState(false);

  // 弹窗状态
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form] = Form.useForm();

  // 加载列表
  const loadDataRef = useRef<(() => Promise<void>) | null>(null);
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await AiModelProviderApi.list();
      setDataSource(result || []);
    } finally {
      setLoading(false);
    }
  }, []);

  loadDataRef.current = loadData;

  useEffect(() => {
    loadData();
  }, [loadData]);

  // 打开创建弹窗
  const openCreateModal = useCallback(() => {
    setEditingId(null);
    form.resetFields();
    // 设置默认值
    form.setFieldsValue({ protocolType: "OPENAI_COMPATIBLE", timeoutMillis: 30000, status: 1 });
    setShowModal(true);
  }, [form]);

  // 打开编辑弹窗
  const openEditModal = useCallback((record: AiModelProviderResponseDto) => {
    setEditingId(record.id);
    form.setFieldsValue({
      providerCode: record.providerCode,
      providerName: record.providerName,
      protocolType: record.protocolType,
      baseUrl: record.baseUrl,
      timeoutMillis: record.timeoutMillis,
      systemDefault: record.systemDefault,
      status: record.status,
      remark: record.remark
    });
    setShowModal(true);
  }, [form]);

  // 提交
  const { onClick: handleSubmit, loading: submitLoading } = usePreventDoubleClickHook(async () => {
    const values = await form.validateFields();
    const data: AiModelProviderSaveRequestDto = {
      ...values,
      id: editingId || undefined,
      apiKey: values.apiKey || undefined
    };
    await AiModelProviderApi.save(data);
    ToastUtil.success(editingId ? "更新成功" : "创建成功");
    setShowModal(false);
    loadDataRef.current?.();
  });

  // 删除
  const handleDelete = useCallback(async (id: string) => {
    await AiModelProviderApi.deleteById(id);
    ToastUtil.success("删除成功");
    loadDataRef.current?.();
  }, []);

  // 表格列
  const columns = useMemo<ColumnsType<AiModelProviderResponseDto>>(() => [
    { title: "供应商编码", dataIndex: "providerCode", width: 120 },
    { title: "供应商名称", dataIndex: "providerName", width: 140 },
    { title: "协议类型", dataIndex: "protocolType", width: 120 },
    { title: "服务地址", dataIndex: "baseUrl", ellipsis: true, render: value => <Tooltip title={value}>{value}</Tooltip> },
    {
      title: "密钥", dataIndex: "apiKeyConfigured", width: 80,
      render: (val: boolean) => val ? <Tag color="green">已配置</Tag> : <Tag color="red">未配置</Tag>
    },
    { title: "超时(ms)", dataIndex: "timeoutMillis", width: 90 },
    {
      title: "默认", dataIndex: "systemDefault", width: 70,
      render: (val: boolean) => val ? <Tag color="blue">是</Tag> : "-"
    },
    {
      title: "状态", dataIndex: "status", width: 70,
      render: (status: number) => <Tooltip title={getStatusLabel(status)}>{getStatusLabel(status)}</Tooltip>
    },
    { title: "备注", dataIndex: "remark", ellipsis: true, render: value => <Tooltip title={value}>{value || "-"}</Tooltip> },
    { title: "修改时间", dataIndex: "updateTime", width: 160 },
    {
      title: "操作", width: 150,
      render: (_: unknown, record: AiModelProviderResponseDto) => (
        <Space>
          <Button type="link" onClick={() => openEditModal(record)}>编辑</Button>
          <Popconfirm title="确定删除该供应商吗？" onConfirm={() => handleDelete(record.id)} okButtonProps={{ danger: true }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ], [openEditModal, handleDelete]);

  return (
    <div>
      <Typography.Title level={3}>模型供应商管理</Typography.Title>

      {/* 操作按钮区 */}
      <div className="simple-toolbar">
        <Space />
        <Button type="primary" onClick={openCreateModal}>新增供应商</Button>
      </div>

      {/* 数据表格 */}
      <Table
        rowKey="id"
        bordered
        columns={columns}
        dataSource={dataSource}
        loading={loading}
        pagination={false}
      />

      {/* 弹窗 */}
      <Modal
        open={showModal}
        onCancel={() => setShowModal(false)}
        title={editingId ? "编辑供应商" : "新增供应商"}
        width={560}
        centered
        footer={[
          <Button key="cancel" onClick={() => setShowModal(false)}>取消</Button>,
          <Button key="confirm" type="primary" loading={submitLoading} onClick={handleSubmit}>
            {editingId ? "确认更新" : "确认创建"}
          </Button>
        ]}
      >
        <div className="simple-form-container">
          <Form form={form} layout="horizontal" size="middle" labelCol={{ span: 6 }} wrapperCol={{ span: 18 }}>
            <Form.Item
              label="供应商编码"
              name="providerCode"
              rules={[{ required: true, message: "请输入供应商编码" }]}
              style={{ marginBottom: 16 }}
            >
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} disabled={!!editingId} />
            </Form.Item>
            <Form.Item
              label="供应商名称"
              name="providerName"
              rules={[{ required: true, message: "请输入供应商名称" }]}
              style={{ marginBottom: 16 }}
            >
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item
              label="协议类型"
              name="protocolType"
              rules={[{ required: true, message: "请选择协议类型" }]}
              style={{ marginBottom: 16 }}
            >
              <Select style={{ height: 36 }} options={[{ label: "OPENAI_COMPATIBLE", value: "OPENAI_COMPATIBLE" }]} />
            </Form.Item>
            <Form.Item
              label="服务地址"
              name="baseUrl"
              rules={[{ required: true, message: "请输入服务地址" }]}
              style={{ marginBottom: 16 }}
            >
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item
              label="API Key"
              name="apiKey"
              rules={editingId ? [] : [{ required: true, message: "请输入API Key" }]}
              style={{ marginBottom: 16 }}
            >
              <Input.Password
                style={{ height: 36, fontSize: 14, padding: "0 12px" }}
                placeholder={editingId ? "留空则不修改" : "请输入API Key"}
              />
            </Form.Item>
            <Form.Item
              label="超时(ms)"
              name="timeoutMillis"
              rules={[{ required: true, message: "请输入超时" }]}
              style={{ marginBottom: 16 }}
            >
              <InputNumber style={{ width: "100%", height: 36 }} min={1000} max={300000} />
            </Form.Item>
            <Form.Item label="系统默认" name="systemDefault" valuePropName="checked" style={{ marginBottom: 16 }}>
              <Select style={{ height: 36 }} options={[{ label: "是", value: true }, { label: "否", value: false }]} />
            </Form.Item>
            <Form.Item
              label="状态"
              name="status"
              rules={[{ required: true, message: "请选择状态" }]}
              style={{ marginBottom: 16 }}
            >
              <Select style={{ height: 36 }} options={[{ label: "启用", value: 1 }, { label: "停用", value: 0 }]} />
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
