package com.devoxx.genie.service.prompt.strategy;

import com.devoxx.genie.model.request.ChatMessageContext;
import com.devoxx.genie.model.request.SemanticFile;
import com.devoxx.genie.service.prompt.error.ExecutionException;
import com.devoxx.genie.service.prompt.error.PromptErrorHandler;
import com.devoxx.genie.service.prompt.error.PromptException;
import com.devoxx.genie.service.prompt.memory.ChatMemoryManager;
import com.devoxx.genie.service.prompt.response.nonstreaming.NonStreamingPromptExecutionService;
import com.devoxx.genie.service.prompt.result.PromptResult;
import com.devoxx.genie.service.prompt.threading.PromptTask;
import com.devoxx.genie.service.prompt.threading.ThreadPoolManager;
import com.devoxx.genie.service.rag.SearchResult;
import com.devoxx.genie.service.rag.SemanticSearchService;
import com.devoxx.genie.ui.panel.PromptOutputPanel;
import com.devoxx.genie.ui.topic.AppTopics;
import com.devoxx.genie.ui.util.NotificationUtil;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import static com.devoxx.genie.model.Constant.FIND_COMMAND;
import static com.devoxx.genie.service.MessageCreationService.extractFileReferences;

/**
 * Strategy for executing non-streaming prompts.
 */
@Slf4j
public class NonStreamingPromptStrategy extends AbstractPromptExecutionStrategy {

    protected NonStreamingPromptExecutionService promptExecutionService;

    public NonStreamingPromptStrategy(Project project) {
        super(project);
        this.promptExecutionService = NonStreamingPromptExecutionService.getInstance();
    }
    
    /**
     * Constructor for dependency injection, primarily used for testing.
     *
     * @param project The IntelliJ project
     * @param chatMemoryManager The chat memory manager
     * @param threadPoolManager The thread pool manager
     * @param promptExecutionService The non-streaming prompt execution service
     */
    protected NonStreamingPromptStrategy(
            @NotNull Project project,
            @NotNull ChatMemoryManager chatMemoryManager,
            @NotNull ThreadPoolManager threadPoolManager,
            @NotNull NonStreamingPromptExecutionService promptExecutionService) {
        super(project, chatMemoryManager, threadPoolManager);
        this.promptExecutionService = promptExecutionService;
    }

    @Override
    protected String getStrategyName() {
        return "non-streaming prompt";
    }

    @Override
    protected void executeStrategySpecific(
            @NotNull ChatMessageContext context,
            @NotNull PromptOutputPanel panel,
            @NotNull PromptTask<PromptResult> resultTask) {
            
        // Handle FIND command separately
        if (FIND_COMMAND.equalsIgnoreCase(context.getCommandName())) {
            log.debug("Executing find command");
            executeSemanticSearch(context, panel, resultTask);
            return;
        }

        // Prepare memory and add user message
        prepareMemory(context);

        // Execute the prompt using the centralized thread pool
        threadPoolManager.getPromptExecutionPool().execute(() -> {
            try {
                // Record start time
                long startTime = System.currentTimeMillis();
                
                // Execute the query
                var response = promptExecutionService.executeQuery(context).get();
                
                if (response == null) {
                    resultTask.complete(PromptResult.failure(context, 
                        new ExecutionException("Null response received")));
                    return;
                }
                
                log.debug("Adding AI message to prompt output panel for context {}", context.getId());
                context.setAiMessage(response.aiMessage());
                context.setExecutionTimeMs(System.currentTimeMillis() - startTime);

                // Set token usage and cost
                context.setTokenUsageAndCost(response.tokenUsage());

                // Add the conversation to the chat service
                project.getMessageBus()
                        .syncPublisher(AppTopics.CONVERSATION_TOPIC)
                        .onNewConversation(context);

                panel.addChatResponse(context);
                resultTask.complete(PromptResult.success(context));
            } catch (Exception e) {
                if (e instanceof CancellationException || 
                    e.getCause() instanceof CancellationException || 
                    Thread.currentThread().isInterrupted()) {
                    log.info("Prompt execution cancelled for context {}", context.getId());
                    resultTask.cancel(true);
                } else {
                    handleExecutionError(e, context, resultTask);
                }
            }
        });
        
        // Additional cancellation handling for non-streaming strategy
        resultTask.whenComplete((result, error) -> {
            if (resultTask.isCancelled()) {
                log.debug("Task cancelled, cancelling prompt execution");
                promptExecutionService.cancelExecutingQuery();
            }
        });
    }

    /**
     * Cancel the current prompt execution.
     */
    @Override
    public void cancel() {
        promptExecutionService.cancelExecutingQuery();
    }

    /**
     * Perform semantic search for the FIND command.
     */
    private void executeSemanticSearch(
            @NotNull ChatMessageContext context,
            @NotNull PromptOutputPanel panel,
            @NotNull PromptTask<PromptResult> resultTask) {
        
        threadPoolManager.getPromptExecutionPool().execute(() -> {
            try {
                SemanticSearchService semanticSearchService = SemanticSearchService.getInstance();
                Map<String, SearchResult> searchResults = semanticSearchService.search(
                        context.getProject(),
                        context.getUserPrompt()
                );

                if (!searchResults.isEmpty()) {
                    List<SemanticFile> fileReferences = extractFileReferences(searchResults);
                    context.setSemanticReferences(fileReferences);
                    panel.addChatResponse(context);
                    resultTask.complete(PromptResult.success(context));
                } else {
                    NotificationUtil.sendNotification(context.getProject(),
                            "No relevant files found for your search query.");
                    resultTask.complete(PromptResult.failure(context, 
                        new ExecutionException("No relevant files found")));
                }
            } catch (Exception e) {
                // Create a specific execution exception for semantic search errors
                ExecutionException searchError = new ExecutionException(
                    "Error performing semantic search", e, 
                    PromptException.ErrorSeverity.WARNING, true);
                PromptErrorHandler.handleException(context.getProject(), searchError, context);
                resultTask.complete(PromptResult.failure(context, searchError));
            }
        });
    }
}
