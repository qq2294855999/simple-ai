package com.simple.ai.view.agentChat;

import com.simple.ai.view.agentChatMessage.AgentChatMessageRepository;
import com.simple.ai.view.agentChatSession.AgentChatSessionRepository;
import com.simple.ai.view.agentDefinition.AgentDefinitionRepository;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 聊天持久化 Mapper SQL 契约测试。
 *
 * @author qty
 */
class AgentChatMapperContractTest {

    /**
     * 验证会话与消息查询使用真实 Repository 方法、参数化条件、排序、上限和行锁。
     *
     * @throws Exception XML 解析失败时抛出
     */
    @Test
    void chatQueryMappersShouldKeepRepositoryBoundOrderingLimitAndRowLock() throws Exception {
        Map<String, String> sessionSql = loadMapperSql("mapper/AgentChatSessionDao.xml", AgentChatSessionRepository.class);
        Map<String, String> messageSql = loadMapperSql("mapper/AgentChatMessageDao.xml", AgentChatMessageRepository.class);

        String sessionLockSql = sessionSql.get("selectByIdForUpdate");
        String sessionListSql = sessionSql.get("selectAllByAgentId");
        String messageListSql = messageSql.get("selectAllBySessionId");
        String maxSequenceSql = messageSql.get("selectMaxSequenceNo");

        assertTrue(sessionLockSql.contains("where id = #{id}"));
        assertTrue(sessionLockSql.endsWith("for update"));
        assertTrue(sessionListSql.contains("where agent_id = #{agentid}"));
        assertTrue(sessionListSql.contains("order by last_message_at desc"));
        assertTrue(sessionListSql.contains("limit 100"));
        assertTrue(messageListSql.contains("where session_id = #{sessionid}"));
        assertTrue(messageListSql.contains("order by sequence_no asc"));
        assertTrue(messageListSql.contains("limit 1000"));
        assertTrue(maxSequenceSql.contains("coalesce(max(sequence_no), 0)"));
        assertTrue(maxSequenceSql.contains("where session_id = #{sessionid}"));
    }

    /**
     * 验证删除聊天数据的 SQL 仅处理传入智能体所属会话，且删除顺序可保持消息先于会话。
     *
     * @throws Exception XML 解析失败时抛出
     */
    @Test
    void cascadeDeleteMapperShouldFilterByIdsAndDeleteMessagesBeforeSessions() throws Exception {
        Map<String, String> mapperSql = loadMapperSql("mapper/AgentDefinitionDao.xml", AgentDefinitionRepository.class);
        String messageDeleteSql = mapperSql.get("deleteChatMessageByAgentIds");
        String sessionDeleteSql = mapperSql.get("deleteChatSessionByAgentIds");

        assertTrue(messageDeleteSql.startsWith("delete from agent_chat_message"));
        assertTrue(messageDeleteSql.contains("where session_id in"));
        assertTrue(messageDeleteSql.contains("select id from agent_chat_session"));
        assertTrue(messageDeleteSql.contains("where agent_id in #{id}"));
        assertTrue(sessionDeleteSql.startsWith("delete from agent_chat_session"));
        assertTrue(sessionDeleteSql.contains("where agent_id in #{id}"));
        assertFalse(messageDeleteSql.matches("delete from agent_chat_message\\s*"));
        assertFalse(sessionDeleteSql.matches("delete from agent_chat_session\\s*"));
    }

    /**
     * 解析 Mapper XML 并校验 namespace、语句标识与 Repository 方法一致。
     *
     * @param resource Mapper 资源路径
     * @param repositoryType Repository 类型
     * @return 归一化后的 SQL 映射
     * @throws Exception XML 解析失败时抛出
     */
    private Map<String, String> loadMapperSql(String resource, Class<?> repositoryType) throws Exception {
        Document document = loadDocument(resource);
        Element root = document.getDocumentElement();
        Map<String, String> statements = new HashMap<>();

        assertEquals(repositoryType.getName(), root.getAttribute("namespace"));

        // 每条 SQL 都必须对应 Repository 的真实方法，防止 XML id 漂移
        NodeList nodes = root.getChildNodes();
        for (int index = 0; index < nodes.getLength(); index++) {
            Node node = nodes.item(index);
            if (!(node instanceof Element element)) {
                continue;
            }
            String id = element.getAttribute("id");
            if (id.isBlank()) {
                continue;
            }
            assertTrue(hasMethod(repositoryType, id));
            statements.put(id, normalizeSql(element.getTextContent()));
        }
        return statements;
    }

    /**
     * 加载 XML 资源文档。
     *
     * @param resource Mapper 资源路径
     * @return XML 文档
     * @throws Exception XML 解析失败时抛出
     */
    private Document loadDocument(String resource) throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertTrue(inputStream != null, "未找到 Mapper 资源：" + resource);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();

            // MyBatis Mapper 保留必要的 DOCTYPE，但测试解析禁止加载外部 DTD
            builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
            return builder.parse(inputStream);
        }
    }

    /**
     * 判断 Repository 是否定义了指定方法。
     *
     * @param repositoryType Repository 类型
     * @param methodName 方法名
     * @return 是否存在方法
     */
    private boolean hasMethod(Class<?> repositoryType, String methodName) {
        for (Method method : repositoryType.getMethods()) {

            // Mapper 语句仅允许绑定本 Repository 已声明的方法
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 归一化 XML 文本中的空白字符。
     *
     * @param sql 原始 SQL 文本
     * @return 单空格连接的小写 SQL
     */
    private String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
