-- ============================================================
-- 人机会话：agent_chat_message 新增思考过程字段
-- 日期: 2026-07-26
-- 说明: 为 AI 回复消息增加思考推理过程的持久化存储
--       框架先行，AI 模型 SDK 支持 reasoning content 后直接复用
-- ============================================================

-- 思考过程完整文本（AI reasoning 内容），可能数千字，使用 TEXT
ALTER TABLE agent_chat_message
    ADD COLUMN thinking_content TEXT DEFAULT '' NOT NULL;
COMMENT
ON COLUMN agent_chat_message.thinking_content IS 'AI 思考推理过程完整文本（reasoning content）';

-- 思考内容格式，与 content_format 对应（PLAIN_TEXT / RESTRICTED_MARKDOWN）
ALTER TABLE agent_chat_message
    ADD COLUMN thinking_content_format VARCHAR(32) DEFAULT 'PLAIN_TEXT' NOT NULL;
COMMENT
ON COLUMN agent_chat_message.thinking_content_format IS '思考内容格式: PLAIN_TEXT / RESTRICTED_MARKDOWN';