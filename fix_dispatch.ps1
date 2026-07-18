$f = 'src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java'
$c = [System.IO.File]::ReadAllText($f, [System.Text.Encoding]::UTF8)

# Fix the broken summarize method - remove entire method body and just return
$old = "    private String summarizeMemoryAfterGoalAchieved(Task task, CommandDispatchRequest request,
                                                    Consumer<CommandDispatchProgressEvent> progressConsumer,
                                                    String responseContent) {

        publishProgress(progressConsumer, request, task, `"MEMORY_SUMMARIZING`", `"正在沉淀智能体记忆`", `"`", Boolean.FALSE, `"`");
        String memoryId = // removed.summarize(request.getAgentId(), task.getId(), request.getCommandName());
        persistTaskMemoryId(task, memoryId);
        return responseContent;
    }"

$new = "    private String handleAiExplorationCompletion(Task task, CommandDispatchRequest request,
                                                    Consumer<CommandDispatchProgressEvent> progressConsumer,
                                                    String responseContent) {
        return responseContent;
    }"

$c = $c.Replace($old, $new)
[System.IO.File]::WriteAllText($f, $c, [System.Text.Encoding]::UTF8)
Write-Output 'Fixed'
