package com.simple.ai.service.agentDefinition;

import com.simple.ai.common.copy.agentDefinition.AgentDefinitionCopyMapper;
import com.simple.ai.common.dto.agentDefinition.DeleteCascadeAgentDefinitionResponse;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * 智能体删除聊天数据闭环测试。
 *
 * @author qty
 */
@ExtendWith(MockitoExtension.class)
class DefaultAgentDefinitionServiceChatCascadeTest {

    /** 被测智能体服务 */
    private DefaultAgentDefinitionService agentDefinitionService;

    /** 智能体数据访问视图替身 */
    @Mock
    private AgentDefinitionView agentDefinitionView;

    /** 复制映射替身 */
    @Mock
    private AgentDefinitionCopyMapper copyMapper;

    /**
     * 初始化智能体服务的必要依赖。
     */
    @BeforeEach
    void setUp() {
        agentDefinitionService = new DefaultAgentDefinitionService();
        ReflectionTestUtils.setField(agentDefinitionService, "agentDefinitionView", agentDefinitionView);
        ReflectionTestUtils.setField(agentDefinitionService, "copy", copyMapper);
    }

    /**
     * 验证删除智能体时先删聊天消息再删聊天会话，避免产生聊天孤儿记录。
     */
    @Test
    void deleteCascadeByIdsShouldDeleteChatMessagesBeforeChatSessions() {
        List<String> agentIds = List.of("agent-local");
        DeleteCascadeAgentDefinitionResponse response = new DeleteCascadeAgentDefinitionResponse();
        response.setAgentCount(1L);
        when(agentDefinitionView.countCascadeByIds(agentIds)).thenReturn(response);

        DeleteCascadeAgentDefinitionResponse actual = agentDefinitionService.deleteCascadeByIds(agentIds);

        assertSame(response, actual);
        InOrder order = inOrder(agentDefinitionView);
        order.verify(agentDefinitionView, times(2)).countCascadeByIds(agentIds);
        order.verify(agentDefinitionView).deleteChatMessageByAgentIds(agentIds);
        order.verify(agentDefinitionView).deleteChatSessionByAgentIds(agentIds);
        order.verify(agentDefinitionView).unlinkAtomicCommandSkillByAgentIds(agentIds);
        order.verify(agentDefinitionView).deleteTaskDetailByAgentIds(agentIds);
        order.verify(agentDefinitionView).deleteTaskByAgentIds(agentIds);
        order.verify(agentDefinitionView).deleteMemoryDetailByAgentIds(agentIds);
        order.verify(agentDefinitionView).deleteMemoryByAgentIds(agentIds);
        order.verify(agentDefinitionView).deleteSubAgentRelationByAgentIds(agentIds);
        order.verify(agentDefinitionView).deleteRuleByAgentIds(agentIds);
        order.verify(agentDefinitionView).deleteSkillByAgentIds(agentIds);
        order.verify(agentDefinitionView).deleteByIds(agentIds);
    }
}
