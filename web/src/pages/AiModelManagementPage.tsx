import {Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag, Tooltip, Typography} from "antd";
import type {ColumnsType} from "antd/es/table";
import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {usePreventDoubleClickHook} from "../hooks/usePreventDoubleClickHook";
import {ToastUtil} from "../utils/ToastUtil";
import {AiModelApi} from "../api/aiModelApi";
import {AiModelProviderApi} from "../api/aiModelProviderApi";
import type {AiModelProviderModelDto, AiModelResponseDto, AiModelSaveRequestDto} from "../dto/aiModel/AiModelDto";
import type {AiModelProviderResponseDto} from "../dto/aiModelProvider/AiModelProviderDto";

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
 * AI 模型管理页面。
 *
 * @author qty
 */
export function AiModelManagementPage() {
  const [dataSource, setDataSource] = useState<AiModelResponseDto[]>([]);
  const [loading, setLoading] = useState(false);

  // 弹窗状态
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form] = Form.useForm();

  // 供应商下拉
  const [providers, setProviders] = useState<AiModelProviderResponseDto[]>([]);

  // 远程模型列表
  const [providerModels, setProviderModels] = useState<AiModelProviderModelDto[]>([]);
  const [fetchingModels, setFetchingModels] = useState(false);

  // 监听供应商变化
  const watchedProviderId = Form.useWatch("providerId", form);

  // 加载供应商下拉
  const loadProviders = useCallback(async () => {
    try {
      const result = await AiModelProviderApi.list();
      setProviders(result || []);
    } catch {
      // 下拉加载失败不影响主流程
    }
  }, []);

  // 加载列表
  const loadDataRef = useRef<(() => Promise<void>) | null>(null);
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await AiModelApi.list();
      setDataSource(result || []);
    } finally {
      setLoading(false);
    }
  }, []);

  loadDataRef.current = loadData;

  useEffect(() => {
    loadData();
    loadProviders();
  }, [loadData, loadProviders]);

  // 供应商切换时自动拉取远程模型列表
  useEffect(() => {
    if (!watchedProviderId) {
      setProviderModels([]);
      return;
    }
    let cancelled = false;
    const fetchModels = async () => {
      setFetchingModels(true);
      try {
        const models = await AiModelApi.fetchProviderModels(watchedProviderId);
        if (!cancelled) {
          setProviderModels(models || []);
        }
      } catch {
        if (!cancelled) {
          setProviderModels([]);
        }
      } finally {
        if (!cancelled) {
          setFetchingModels(false);
        }
      }
    };
    fetchModels();
    return () => { cancelled = true; };
  }, [watchedProviderId, form]);

  // 打开创建弹窗
  const openCreateModal = useCallback(() => {
    setEditingId(null);
    form.resetFields();
    form.setFieldsValue({ status: 1 });
    setShowModal(true);
  }, [form]);

  // 打开编辑弹窗
  const openEditModal = useCallback((record: AiModelResponseDto) => {
    setEditingId(record.id);
    form.setFieldsValue({
      providerId: record.providerId,
      modelCode: record.modelCode,
      modelName: record.modelName,
      capabilityConfig: record.capabilityConfig,
      contextWindow: record.contextWindow,
      providerDefault: record.providerDefault,
      systemDefault: record.systemDefault,
      status: record.status,
      remark: record.remark
    });
    setShowModal(true);
  }, [form]);

  // 提交
  const { onClick: handleSubmit, loading: submitLoading } = usePreventDoubleClickHook(async () => {
    const values = await form.validateFields();
    const data: AiModelSaveRequestDto = {
      ...values,
      id: editingId || undefined
    };
    await AiModelApi.save(data);
    ToastUtil.success(editingId ? "更新成功" : "创建成功");
    setShowModal(false);
    loadDataRef.current?.();
  });

  // 删除
  const handleDelete = useCallback(async (id: string) => {
    await AiModelApi.deleteById(id);
    ToastUtil.success("删除成功");
    loadDataRef.current?.();
  }, []);

  // 表格列
  const columns = useMemo<ColumnsType<AiModelResponseDto>>(() => [
    { title: "供应商", dataIndex: "providerName", width: 120 },
    { title: "模型编码", dataIndex: "modelCode", width: 120 },
    { title: "模型名称", dataIndex: "modelName", width: 140 },
    { title: "上下文窗口", dataIndex: "contextWindow", width: 100 },
    {
      title: "供应商默认", dataIndex: "providerDefault", width: 90,
      render: (val: boolean) => val ? <Tag color="blue">是</Tag> : "-"
    },
    {
      title: "系统默认", dataIndex: "systemDefault", width: 90,
      render: (val: boolean) => val ? <Tag color="blue">是</Tag> : "-"
    },
    {
      title: "状态", dataIndex: "status", width: 70,
        render: (status: number) => <Tag color={getStatusLabel(status) === "启用" ? "green" : "red"}>{getStatusLabel(status)}</Tag>
    },
    { title: "备注", dataIndex: "remark", ellipsis: true, render: value => <Tooltip title={value}>{value || "-"}</Tooltip> },
    { title: "修改时间", dataIndex: "updateTime", width: 160 },
    {
      title: "操作", width: 150,
      render: (_: unknown, record: AiModelResponseDto) => (
        <Space>
          <Button type="link" onClick={() => openEditModal(record)}>编辑</Button>
          <Popconfirm title="确定删除该模型吗？" onConfirm={() => handleDelete(record.id)} okButtonProps={{ danger: true }}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ], [openEditModal, handleDelete]);

  return (
    <div>
      <Typography.Title level={3}>模型管理</Typography.Title>

      {/* 操作按钮区 */}
      <div className="simple-toolbar">
        <Space />
        <Button type="primary" onClick={openCreateModal}>新增模型</Button>
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
        title={editingId ? "编辑模型" : "新增模型"}
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
              label="供应商"
              name="providerId"
              rules={[{ required: true, message: "请选择供应商" }]}
              style={{ marginBottom: 16 }}
            >
              <Select
                placeholder="选择供应商"
                style={{ height: 36 }}
                options={providers.map(p => ({ label: p.providerName, value: p.id }))}
                showSearch
                filterOption={(input, option) => (option?.label as string || "").includes(input)}
              />
            </Form.Item>
            <Form.Item
              label="模型编码"
              name="modelCode"
              rules={[{ required: true, message: "请选择或输入模型编码" }]}
              style={{ marginBottom: 16 }}
            >
              <Select
                placeholder="选择供应商后自动获取，也可手动输入"
                style={{ height: 36 }}
                loading={fetchingModels}
                showSearch
                filterOption={(input, option) => (option?.label as string || "").includes(input)}
                options={providerModels.map(m => ({ label: m.modelCode, value: m.modelCode }))}
                onChange={(value: string) => {
                  const selected = providerModels.find(m => m.modelCode === value);
                  if (selected) {
                    form.setFieldsValue({ modelName: selected.modelName });
                  }
                }}
              />
            </Form.Item>
            <Form.Item
              label="模型名称"
              name="modelName"
              rules={[{ required: true, message: "请输入模型名称" }]}
              style={{ marginBottom: 16 }}
            >
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="能力配置" name="capabilityConfig" style={{ marginBottom: 16 }}>
              <Input style={{ height: 36, fontSize: 14, padding: "0 12px" }} />
            </Form.Item>
            <Form.Item label="上下文窗口" name="contextWindow" style={{ marginBottom: 16 }}>
              <InputNumber style={{ width: "100%", height: 36 }} min={1} placeholder="如 4096" />
            </Form.Item>
            <Form.Item label="供应商默认" name="providerDefault" valuePropName="checked" style={{ marginBottom: 16 }}>
              <Select style={{ height: 36 }} options={[{ label: "是", value: true }, { label: "否", value: false }]} />
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
