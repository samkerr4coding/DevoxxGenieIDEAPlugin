package com.axa.jetbrains.controller;

import com.axa.jetbrains.chatmodel.ChatModelProvider;
import com.axa.jetbrains.controller.listener.PromptExecutionListener;
import com.axa.jetbrains.model.LanguageModel;
import com.axa.jetbrains.model.enumarations.ModelProvider;
import com.axa.jetbrains.model.request.ChatMessageContext;
import com.axa.jetbrains.service.AxaAiSettingsService;
import com.axa.jetbrains.ui.EditorFileButtonManager;
import com.axa.jetbrains.ui.component.input.PromptInputArea;
import com.axa.jetbrains.ui.panel.ActionButtonsPanel;
import com.axa.jetbrains.ui.panel.PromptOutputPanel;
import com.axa.jetbrains.ui.settings.AxaAiStateService;
import com.axa.jetbrains.ui.util.NotificationUtil;
import com.axa.jetbrains.util.ChatMessageContextUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.axa.jetbrains.model.enumarations.ModelProvider.*;

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

        ChatMessageContext currentChatMessageContext =
                ChatMessageContextUtil.createContext(project,
                        userPromptText,
                        getSelectedLanguageModel(),
                        chatModelProvider,
                        actionCommand,
                        editorFileButtonManager,
                        projectContext,
                        isProjectContextAdded);

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
        AxaAiStateService stateService = AxaAiStateService.getInstance();
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
    private LanguageModel createDefaultLanguageModel(@NotNull AxaAiSettingsService stateService) {
        ModelProvider selectedProvider = (ModelProvider) modelProviderComboBox.getSelectedItem();
        if (selectedProvider != null &&
                (selectedProvider.equals(LMStudio) ||
                 selectedProvider.equals(GPT4All) ||
                 selectedProvider.equals(LLaMA))) {
            return LanguageModel.builder()
                    .provider(selectedProvider)
                    .apiKeyUsed(false)
                    .inputCost(0)
                    .outputCost(0)
                    .contextWindow(4096)
                    .build();
        } else {
            String modelName = stateService.getSelectedLanguageModel(project.getLocationHash());
            return LanguageModel.builder()
                    .provider(selectedProvider != null ? selectedProvider : OpenAI)
                    .modelName(modelName)
                    .apiKeyUsed(false)
                    .inputCost(0)
                    .outputCost(0)
                    .contextWindow(128_000)
                    .build();
        }
    }

    /**
     * Initiates the calculation of tokens and cost based on the selected model and provider.
     * It delegates the actual calculation to the TokenCalculationController.
     */
    public void calculateTokensAndCost() {
        tokenCalculationController.calculateTokensAndCost();
    }

    public void updateButtonVisibility() {
        boolean isSupported = projectContextController.isProjectContextSupportedProvider();
        actionButtonsPanel.setCalcTokenCostButtonVisible(isSupported);
        actionButtonsPanel.setAddProjectButtonVisible(isSupported);
    }
}
