import { Navigate, createBrowserRouter } from "react-router-dom";
import { BasicLayoutComponent } from "../components/layout/BasicLayoutComponent";
import { AgentWorkbenchPage } from "../pages/AgentWorkbenchPage";
import { AgentDesignManagementPage } from "../pages/AgentDesignManagementPage";
import { CommandDispatchPage } from "../pages/CommandDispatchPage";
import { AgentSkillManagementPage } from "../pages/AgentSkillManagementPage";
import { AgentRuleManagementPage } from "../pages/AgentRuleManagementPage";
import { SubAgentRelationManagementPage } from "../pages/SubAgentRelationManagementPage";
import { AgentMemoryManagementPage } from "../pages/AgentMemoryManagementPage";
import { AtomicCommandManagementPage } from "../pages/AtomicCommandManagementPage";
import { TaskManagementPage } from "../pages/TaskManagementPage";

export const AppRouter = createBrowserRouter([
  {
    path: "/",
    element: <BasicLayoutComponent />,
    children: [
      { index: true, element: <Navigate to="/workbench" replace /> },
      { path: "workbench", element: <AgentWorkbenchPage /> },
      { path: "agent-design", element: <AgentDesignManagementPage /> },
      { path: "command-dispatch", element: <CommandDispatchPage /> },
      { path: "agent-skill", element: <AgentSkillManagementPage /> },
      { path: "agent-rule", element: <AgentRuleManagementPage /> },
      { path: "sub-agent-relation", element: <SubAgentRelationManagementPage /> },
      { path: "agent-memory", element: <AgentMemoryManagementPage /> },
      { path: "atomic-command", element: <AtomicCommandManagementPage /> },
      { path: "task", element: <TaskManagementPage /> }
    ]
  }
]);
