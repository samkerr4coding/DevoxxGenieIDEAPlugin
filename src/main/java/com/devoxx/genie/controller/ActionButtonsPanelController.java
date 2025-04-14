package com.devoxx.genie.controller;

import com.devoxx.genie.chatmodel.ChatModelProvider;
import com.devoxx.genie.controller.listener.PromptExecutionListener;
import com.devoxx.genie.model.ChatContextParameters;
import com.devoxx.genie.model.LanguageModel;
import com.devoxx.genie.model.enums.ModelProvider;
import com.devoxx.genie.model.request.ChatMessageContext;
import com.devoxx.genie.service.DevoxxGenieSettingsService;
import com.devoxx.genie.ui.component.button.EditorFileButtonManager;
import com.devoxx.genie.ui.component.input.PromptInputArea;
import com.devoxx.genie.ui.panel.ActionButtonsPanel;
import com.devoxx.genie.ui.panel.PromptOutputPanel;
import com.devoxx.genie.ui.processor.CommandProcessor;
import com.devoxx.genie.ui.settings.DevoxxGenieStateService;
import com.devoxx.genie.ui.util.NotificationUtil;
import com.devoxx.genie.util.ChatMessageContextUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.devoxx.genie.model.enums.ModelProvider.AzureOpenAI;

public class ActionButtonsPanelController implements PromptExecutionListener {

    private final Project project;
    private final EditorFileButtonManager editorFileButtonManager;
    private final PromptInputArea promptInputArea;
    private final ComboBox<ModelProvider> modelProviderComboBox;
    private final ComboBox<LanguageModel> modelNameComboBox;
    private final ChatModelProvider chatModelProvider = new ChatModelProvider();
    private final ActionButtonsPanel actionButtonsPanel;
    private final PromptExecutionController promptExecutionController;
    private final ProjectContextController projectContextController;
    private final TokenCalculationController tokenCalculationController;

    public ActionButtonsPanelController(Project project,
                                        PromptInputArea promptInputArea,
                                        PromptOutputPanel promptOutputPanel,
                                        ComboBox<ModelProvider> modelProviderComboBox,
                                        ComboBox<LanguageModel> modelNameComboBox,
                                        ActionButtonsPanel actionButtonsPanel) {

        this.project = project;
        this.promptInputArea = promptInputArea;
        this.editorFileButtonManager = new EditorFileButtonManager(project, null);
        this.modelProviderComboBox = modelProviderComboBox;
        this.modelNameComboBox = modelNameComboBox;
        this.actionButtonsPanel = actionButtonsPanel;
        this.promptExecutionController = new PromptExecutionController(project, promptInputArea, promptOutputPanel, actionButtonsPanel);
        this.projectContextController = new ProjectContextController(project, modelProviderComboBox, modelNameComboBox, actionButtonsPanel);
        this.tokenCalculationController = new TokenCalculationController(project, modelProviderComboBox, modelNameComboBox, actionButtonsPanel);
    }

    public boolean isPromptRunning() {
        return promptExecutionController.isPromptRunning();
    }

    public boolean handlePromptSubmission(String actionCommand,
                                          boolean isProjectContextAdded,
                                          String projectContext) {

        String userPromptText = getUserPromptText();

        if (userPromptText == null) {
            return false;
        }

        // Check if this is the special /init command that should be handled locally
        if (CommandProcessor.processCommand(project, userPromptText)) {
            // Command was processed, clear the input field
            promptInputArea.clear();
            return false;
        }

        ChatMessageContext currentChatMessageContext = ChatMessageContextUtil.createContext(
                new ChatContextParameters(
                        project,
                        userPromptText,
                        getSelectedLanguageModel(),
                        chatModelProvider,
                        actionCommand,
                        editorFileButtonManager,
                        projectContext,
                        isProjectContextAdded
                )
        );

        return promptExecutionController.handlePromptSubmission(currentChatMessageContext);
    }

    /**
     * Stop the prompt execution.
     */
    @Override
    public void stopPromptExecution() {
        promptExecutionController.stopPromptExecution();
    }

    @Override
    public void startPromptExecution() {
        promptExecutionController.startPromptExecution();
    }

    @Override
    public void endPromptExecution() {
        promptExecutionController.endPromptExecution();
    }

    private LanguageModel getSelectedLanguageModel() {
        DevoxxGenieStateService stateService = DevoxxGenieStateService.getInstance();
        LanguageModel selectedLanguageModel = (LanguageModel) modelNameComboBox.getSelectedItem();

        // If selectedLanguageModel is null, create a default one
        if (selectedLanguageModel == null) {
            selectedLanguageModel = createDefaultLanguageModel(stateService);
        }
        return selectedLanguageModel;
    }

    /**
     * get the user prompt text.
     */
    private @Nullable String getUserPromptText() {
        String userPromptText = promptInputArea.getText();
        if (userPromptText.isEmpty()) {
            NotificationUtil.sendNotification(project, "Please enter a prompt.");
            return null;
        }
        return userPromptText;
    }

    /**
     * Create a default language model.
     *
     * @param stateService the state service
     * @return the default language model
     */
    private LanguageModel createDefaultLanguageModel(@NotNull DevoxxGenieSettingsService stateService) {
        ModelProvider selectedProvider = (ModelProvider) modelProviderComboBox.getSelectedItem();

        String modelName = stateService.getSelectedLanguageModel(project.getLocationHash());
        return LanguageModel.builder()
                .provider(selectedProvider != null ? selectedProvider : AzureOpenAI)
                .modelName(modelName)
                .apiKeyUsed(false)
                .inputCost(0)
                .outputCost(0)
                .inputMaxTokens(128_000)
                .build();
    }

    /**
     * Initiates the calculation of tokens and cost based on the selected model and provider.
     * It delegates the actual calculation to the TokenCalculationController.
     * This operation is performed in a background task to avoid EDT freezes.
     */
    public void calculateTokensAndCost() {
        // Create and run a background task for token calculation
        com.intellij.openapi.progress.ProgressManager.getInstance().run(
            new com.intellij.openapi.progress.Task.Backgroundable(project, "Calculating tokens", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    tokenCalculationController.calculateTokensAndCost();
                }
            }
        );
    }

    public void updateButtonVisibility() {
        boolean isSupported = projectContextController.isProjectContextSupportedProvider();
        actionButtonsPanel.setCalcTokenCostButtonVisible(isSupported);
        actionButtonsPanel.setAddProjectButtonVisible(isSupported);
    }
}
