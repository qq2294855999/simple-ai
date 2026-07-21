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
import {AgentClientManagementPage} from "../pages/AgentClientManagementPage";
import {AgentMemoryVersionPage} from "../pages/AgentMemoryVersionPage";
import {AtomicCommandManagementPage} from "../pages/AtomicCommandManagementPage";
import {TaskManagementPage} from "../pages/TaskManagementPage";
import {AiModelProviderManagementPage} from "../pages/AiModelProviderManagementPage";
import {AiModelManagementPage} from "../pages/AiModelManagementPage";

export const AppRouter = createBrowserRouter([
  {
    path: "/",
    element: <BasicLayoutComponent />,
    children: [
      { index: true, element: <Navigate to="/workbench" replace /> },
      { path: "workbench", element: <AgentWorkbenchPage /> },
      { path: "agent-chat", element: <AgentChatPage /> },
      { path: "agent-design", element: <AgentDesignManagementPage /> },
      { path: "command-dispatch", element: <CommandDispatchPage /> },
      { path: "agent-skill", element: <AgentSkillManagementPage /> },
      { path: "agent-rule", element: <AgentRuleManagementPage /> },
      { path: "sub-agent-relation", element: <SubAgentRelationManagementPage /> },
      { path: "agent-memory", element: <AgentMemoryManagementPage /> },
        {path: "agent-executor", element: <AgentExecutorManagementPage/>},
        {path: "agent-client", element: <AgentClientManagementPage/>},
        {path: "agent-memory-version", element: <AgentMemoryVersionPage/>},
      { path: "atomic-command", element: <AtomicCommandManagementPage /> },
      { path: "task", element: <TaskManagementPage /> },
      { path: "ai-model-provider", element: <AiModelProviderManagementPage /> },
      { path: "ai-model", element: <AiModelManagementPage /> }
    ]
  }
]);
