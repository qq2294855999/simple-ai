- generic [ref=f4e3]:
    - banner [ref=f4e4]:
        - generic [ref=f4e5]:
            - button "收起侧边栏" [ref=f4e6] [cursor=pointer]
            - generic [ref=f4e9]: Simple AI 管理端
            - generic [ref=f4e14]: 智能体工作台
        - generic [ref=f4e17]:
            - generic [ref=f4e18]: 超级管理员
            - button "超级管理员" [ref=f4e20] [cursor=pointer]:
                - img "超级管理员" [ref=f4e23]
    - generic [ref=f4e26]:
        - complementary [ref=f4e27]:
            - menu [ref=f4e29]:
                - menuitem "robot 智能体工作台" [ref=f4e30] [cursor=pointer]:
                    - img "robot" [ref=f4e31]
                    - generic [ref=f4e34]: 智能体工作台
                - menuitem "message 人机对话" [ref=f4e35] [cursor=pointer]:
                    - img "message" [ref=f4e36]
                    - generic [ref=f4e39]: 人机对话
                - menuitem "setting 智能体配置" [ref=f4e40] [cursor=pointer]:
                    - img "setting" [ref=f4e41]
                    - generic [ref=f4e44]: 智能体配置
                - menuitem "api 执行器与客户端" [ref=f4e45] [cursor=pointer]:
                    - img "api" [ref=f4e46]
                    - generic [ref=f4e49]: 执行器与客户端
                - menuitem "send 命令与执行" [ref=f4e50] [cursor=pointer]:
                    - img "send" [ref=f4e51]
                    - generic [ref=f4e54]: 命令与执行
                - menuitem "cloud-server 大模型管理" [ref=f4e55] [cursor=pointer]:
                    - img "cloud-server" [ref=f4e56]
                    - generic [ref=f4e61]: 大模型管理
        - main [ref=f4e63]:
            - generic [ref=f4e64]:
                - heading "智能体工作台" [level=3] [ref=f4e65]
                - generic [ref=f4e66]:
                    - generic [ref=f4e68]:
                        - generic [ref=f4e69]: 智能体数量
                        - generic [ref=f4e72]: "1"
                    - generic [ref=f4e74]:
                        - generic [ref=f4e75]: 启用智能体
                        - generic [ref=f4e78]: "1"
                    - generic [ref=f4e80]:
                        - generic [ref=f4e81]: 技能数量
                        - generic [ref=f4e84]: "0"
                    - generic [ref=f4e86]:
                        - generic [ref=f4e87]: 运行任务
                        - generic [ref=f4e90]: "0"
                    - generic [ref=f4e92]:
                        - generic [ref=f4e93]: 失败待排查
                        - generic [ref=f4e96]: "3"
                - generic [ref=f4e97]:
                    - generic [ref=f4e98]: 近期任务
                    - button "刷 新" [ref=f4e99] [cursor=pointer]
                - table [ref=f4e107]:
                    - rowgroup [ref=f4e114]:
                        - row "智能体名称 任务名称 执行状态 失败原因/备注 更新时间" [ref=f4e115]
                    - rowgroup [ref=f4e121]:
                        - 'row "软件控制 人机对话 执行失败 ### Error querying database. Cause: org.postgresql.util.PSQLException: 错误: 操作符不存在: character
                          varying = integer 建议：没有匹配指定名称和参数类型的操作符. 您也许需要增加明确的类型转换. 位置：203 ### The error may exist in
                          com/simple/ai/view/agentExecutor/AgentExecutorRepository.java (best guess) ### The error may involve defaultParameterMap ### The error
                          occurred while setting parameters ### SQL: SELECT
                          id,executor_code,executor_name,description,status,create_user_id,create_user_name,create_time,update_user_id,update_user_name,update_time,reserve,remark
                          FROM agent_executor WHERE (status = ?) ### Cause: org.postgresql.util.PSQLException: 错误: 操作符不存在: character varying = integer
                          建议：没有匹配指定名称和参数类型的操作符. 您也许需要增加明确的类型转换. 位置：203 ; bad SQL grammar [] 2026-07-22 11:53:32" [ref=f4e122]
                          '
                        - 'row "软件控制 人机对话 执行失败 ### Error querying database. Cause: org.postgresql.util.PSQLException: 错误: 操作符不存在: character
                          varying = integer 建议：没有匹配指定名称和参数类型的操作符. 您也许需要增加明确的类型转换. 位置：203 ### The error may exist in
                          com/simple/ai/view/agentExecutor/AgentExecutorRepository.java (best guess) ### The error may involve defaultParameterMap ### The error
                          occurred while setting parameters ### SQL: SELECT
                          id,executor_code,executor_name,description,status,create_user_id,create_user_name,create_time,update_user_id,update_user_name,update_time,reserve,remark
                          FROM agent_executor WHERE (status = ?) ### Cause: org.postgresql.util.PSQLException: 错误: 操作符不存在: character varying = integer
                          建议：没有匹配指定名称和参数类型的操作符. 您也许需要增加明确的类型转换. 位置：203 ; bad SQL grammar [] 2026-07-22 11:52:40" [ref=f4e129]
                          '
                        - 'row "软件控制 人机对话 执行失败 ### Error querying database. Cause: org.postgresql.util.PSQLException: 错误: 操作符不存在: character
                          varying = integer 建议：没有匹配指定名称和参数类型的操作符. 您也许需要增加明确的类型转换. 位置：203 ### The error may exist in
                          com/simple/ai/view/agentExecutor/AgentExecutorRepository.java (best guess) ### The error may involve defaultParameterMap ### The error
                          occurred while setting parameters ### SQL: SELECT
                          id,executor_code,executor_name,description,status,create_user_id,create_user_name,create_time,update_user_id,update_user_name,update_time,reserve,remark
                          FROM agent_executor WHERE (status = ?) ### Cause: org.postgresql.util.PSQLException: 错误: 操作符不存在: character varying = integer
                          建议：没有匹配指定名称和参数类型的操作符. 您也许需要增加明确的类型转换. 位置：203 ; bad SQL grammar [] 2026-07-22 11:46:56" [ref=f4e136]
                          '