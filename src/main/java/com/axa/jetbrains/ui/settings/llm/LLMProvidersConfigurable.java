package com.axa.jetbrains.ui.settings.llm;

import com.axa.jetbrains.ui.settings.AxaAiStateService;
import com.axa.jetbrains.ui.topic.AppTopics;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.intellij.openapi.options.Configurable.isFieldModified;

public class LLMProvidersConfigurable implements Configurable {

    private final LLMProvidersComponent llmSettingsComponent;

    public LLMProvidersConfigurable() {
        llmSettingsComponent = new LLMProvidersComponent();
    }

    /**
     * Get the display name
     *
     * @return the display name
     */
    @Nls
    @Override
    public String getDisplayName() {
        return "Large Language Models";
    }

    /**
     * Get the Prompt Settings component
     *
     * @return the component
     */
    @Nullable
    @Override
    public JComponent createComponent() {
        return llmSettingsComponent.createPanel();
    }

    /**
     * Check if the settings have been modified
     *
     * @return true if the settings have been modified
     */
    @Override
    public boolean isModified() {
        AxaAiStateService stateService = AxaAiStateService.getInstance();

        boolean isModified = false;

        isModified |= !stateService.getStreamMode().equals(llmSettingsComponent.getStreamModeCheckBox().isSelected());

        isModified |= !stateService.getShowAzureOpenAIFields().equals(llmSettingsComponent.getEnableAzureOpenAICheckBox().isSelected());
        isModified |= isFieldModified(llmSettingsComponent.getAzureOpenAIEndpointField(), stateService.getAzureOpenAIEndpoint());
        isModified |= isFieldModified(llmSettingsComponent.getAzureOpenAIDeploymentField(), stateService.getAzureOpenAIDeployment());
        isModified |= isFieldModified(llmSettingsComponent.getAzureOpenAIKeyField(), stateService.getAzureOpenAIKey());

        isModified |= stateService.getShowAzureOpenAIFields() != llmSettingsComponent.getEnableAzureOpenAICheckBox().isSelected();

        return isModified;
    }

    /**
     * Apply the changes to the settings
     */
    @Override
    public void apply() {
        boolean isModified = isModified();

        AxaAiStateService settings = AxaAiStateService.getInstance();

        settings.setStreamMode(llmSettingsComponent.getStreamModeCheckBox().isSelected());

        settings.setShowAzureOpenAIFields(llmSettingsComponent.getEnableAzureOpenAICheckBox().isSelected());
        settings.setAzureOpenAIEndpoint(llmSettingsComponent.getAzureOpenAIEndpointField().getText());
        settings.setAzureOpenAIDeployment(llmSettingsComponent.getAzureOpenAIDeploymentField().getText());
        settings.setAzureOpenAIKey(new String(llmSettingsComponent.getAzureOpenAIKeyField().getPassword()));

        settings.setShowAzureOpenAIFields(llmSettingsComponent.getEnableAzureOpenAICheckBox().isSelected());

        // Only notify the listener if an API key has changed, so we can refresh the LLM providers list in the UI
        if (isModified) {
            boolean hasKey = (!settings.getAzureOpenAIKey().isBlank() && settings.getShowAzureOpenAIFields());

            ApplicationManager.getApplication().getMessageBus()
                    .syncPublisher(AppTopics.SETTINGS_CHANGED_TOPIC)
                    .settingsChanged(hasKey);
        }
    }

    /**
     * Reset the text area to the default value
     */
    @Override
    public void reset() {
        AxaAiStateService settings = AxaAiStateService.getInstance();

        llmSettingsComponent.getStreamModeCheckBox().setSelected(settings.getStreamMode());

        llmSettingsComponent.getEnableAzureOpenAICheckBox().setSelected(settings.getShowAzureOpenAIFields());
        llmSettingsComponent.getAzureOpenAIEndpointField().setText(settings.getAzureOpenAIEndpoint());
        llmSettingsComponent.getAzureOpenAIDeploymentField().setText(settings.getAzureOpenAIDeployment());
        llmSettingsComponent.getAzureOpenAIKeyField().setText(settings.getAzureOpenAIKey());

        llmSettingsComponent.getEnableAzureOpenAICheckBox().setSelected(settings.getShowAzureOpenAIFields());
    }
}
