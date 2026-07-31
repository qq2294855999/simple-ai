import {createBrowserRouter, Navigate} from "react-router-dom";
import {BasicLayoutComponent} from "../components/layout/BasicLayoutComponent";
import {AgentChatPage} from "../pages/AgentChatPage";
import {AgentWorkbenchPage} from "../pages/AgentWorkbenchPage";
import {AgentDesignManagementPage} from "../pages/AgentDesignManagementPage";
import {CommandDispatchPage} from "../pages/CommandDispatchPage";
import {AgentSkillManagementPage} from "../pages/AgentSkillManagementPage";
import {AgentRuleManagementPage} from "../pages/AgentRuleManagementPage";
import {SubAgentRelationManagementPage} from "../pages/SubAgentRelationManagementPage";
import {AgentMemoryManagementPage} from "../pages/AgentMemoryManagementPage";
import {AgentExecutorManagementPage} from "../pages/AgentExecutorManagementPage";
import {ProtocolManagementPage} from "../pages/ProtocolManagementPage";
import {ProtocolCreatePage} from "../pages/ProtocolCreatePage";
import {ProtocolEditPage} from "../pages/ProtocolEditPage";
import {ProtocolDetailPage} from "../pages/ProtocolDetailPage";
import {AgentClientManagementPage} from "../pages/AgentClientManagementPage";
import {AtomicCommandManagementPage} from "../pages/AtomicCommandManagementPage";
import {TaskManagementPage} from "../pages/TaskManagementPage";
import {AiModelProviderManagementPage} from "../pages/AiModelProviderManagementPage";
import {AiModelManagementPage} from "../pages/AiModelManagementPage";
import {AgentSkillCreatePage} from "../pages/AgentSkillCreatePage";
import {AgentSkillEditPage} from "../pages/AgentSkillEditPage";
import {AgentSkillDetailPage} from "../pages/AgentSkillDetailPage";
import {AgentRuleCreatePage} from "../pages/AgentRuleCreatePage";
import {AgentRuleEditPage} from "../pages/AgentRuleEditPage";
import {AgentRuleDetailPage} from "../pages/AgentRuleDetailPage";
import {AgentDefinitionCreatePage} from "../pages/AgentDefinitionCreatePage";
import {AgentDefinitionEditPage} from "../pages/AgentDefinitionEditPage";
import {AgentDefinitionDetailPage} from "../pages/AgentDefinitionDetailPage";

export const AppRouter = createBrowserRouter([
  {
    path: "/",
    element: <BasicLayoutComponent />,
    children: [
      { index: true, element: <Navigate to="/workbench" replace /> },
      { path: "workbench", element: <AgentWorkbenchPage /> },
      { path: "agent-chat", element: <AgentChatPage /> },
      { path: "agent-design", element: <AgentDesignManagementPage /> },
        {path: "agent-design/create", element: <AgentDefinitionCreatePage/>},
        {path: "agent-design/:id/edit", element: <AgentDefinitionEditPage/>},
        {path: "agent-design/:id", element: <AgentDefinitionDetailPage/>},
      { path: "command-dispatch", element: <CommandDispatchPage /> },
      { path: "agent-skill", element: <AgentSkillManagementPage /> },
        {path: "agent-skill/create", element: <AgentSkillCreatePage/>},
        {path: "agent-skill/:id/edit", element: <AgentSkillEditPage/>},
        {path: "agent-skill/:id", element: <AgentSkillDetailPage/>},
      { path: "agent-rule", element: <AgentRuleManagementPage /> },
        {path: "agent-rule/create", element: <AgentRuleCreatePage/>},
        {path: "agent-rule/:id/edit", element: <AgentRuleEditPage/>},
        {path: "agent-rule/:id", element: <AgentRuleDetailPage/>},
      { path: "sub-agent-relation", element: <SubAgentRelationManagementPage /> },
      { path: "agent-memory", element: <AgentMemoryManagementPage /> },
        {path: "agent-executor", element: <AgentExecutorManagementPage/>},
        {path: "agent-protocol", element: <ProtocolManagementPage/>},
        {path: "agent-protocol/create", element: <ProtocolCreatePage/>},
        {path: "agent-protocol/:id/edit", element: <ProtocolEditPage/>},
        {path: "agent-protocol/:id", element: <ProtocolDetailPage/>},
        {path: "agent-client", element: <AgentClientManagementPage/>},
      { path: "atomic-command", element: <AtomicCommandManagementPage /> },
      { path: "task", element: <TaskManagementPage /> },
      { path: "ai-model-provider", element: <AiModelProviderManagementPage /> },
      { path: "ai-model", element: <AiModelManagementPage /> }
    ]
  }
]);