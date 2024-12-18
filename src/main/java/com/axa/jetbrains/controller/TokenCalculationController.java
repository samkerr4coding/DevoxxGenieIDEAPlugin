package com.axa.jetbrains.controller;

import com.axa.jetbrains.controller.listener.TokenCalculationListener;
import com.axa.jetbrains.model.LanguageModel;
import com.axa.jetbrains.model.enumarations.ModelProvider;
import com.axa.jetbrains.service.TokenCalculationService;
import com.axa.jetbrains.ui.util.NotificationUtil;
import com.axa.jetbrains.util.DefaultLLMSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;

public class TokenCalculationController {

    private final Project project;
    private final ComboBox<ModelProvider> modelProviderComboBox;
    private final ComboBox<LanguageModel> modelNameComboBox;
    private final TokenCalculationService tokenCalculationService;
    private TokenCalculationListener listener;

    public TokenCalculationController(Project project,
                                      ComboBox<ModelProvider> modelProviderComboBox,
                                      ComboBox<LanguageModel> modelNameComboBox,
                                      TokenCalculationListener listener){
        this.project = project;
        this.modelProviderComboBox = modelProviderComboBox;
        this.modelNameComboBox = modelNameComboBox;
        this.tokenCalculationService = new TokenCalculationService();
        this.listener = listener;
    }

    /**
     * Calculates the number of tokens and the associated cost for the current context.
     * Displays the results in the token usage bar.
     */
    public void calculateTokensAndCost() {
        LanguageModel selectedModel = (LanguageModel) modelNameComboBox.getSelectedItem();
        ModelProvider selectedProvider = (ModelProvider) modelProviderComboBox.getSelectedItem();

        if (selectedModel == null || selectedProvider == null) {
            notifyModelOrProviderNotSelected();
            return;
        }

        int maxTokens = selectedModel.getContextWindow();
        boolean isApiKeyBased = DefaultLLMSettingsUtil.isApiKeyBasedProvider(selectedProvider);

        // Perform the token and cost calculation
        tokenCalculationService.calculateTokensAndCost(
                project,
                null, // Assuming file content is not needed for this calculation
                maxTokens,
                selectedProvider,
                selectedModel,
                isApiKeyBased,
                listener
        );
    }

    /**
     * Notifies the user if either the model or provider is not selected.
     */
    private void notifyModelOrProviderNotSelected() {
        if (modelNameComboBox.getSelectedItem() == null) {
            NotificationUtil.sendNotification(project, "Please select a model first");
        } else {
            NotificationUtil.sendNotification(project, "Please select a provider first");
        }
    }
}
