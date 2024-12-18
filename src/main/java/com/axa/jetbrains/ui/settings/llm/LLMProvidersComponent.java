package com.axa.jetbrains.ui.settings.llm;

import com.axa.jetbrains.service.PropertiesService;
import com.axa.jetbrains.ui.settings.AbstractSettingsComponent;
import com.intellij.util.ui.JBUI;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;

public class LLMProvidersComponent extends AbstractSettingsComponent {

    @Getter
    private final JTextField projectVersion = new JTextField(PropertiesService.getInstance().getVersion());
    @Getter
    private final JTextField azureOpenAIEndpointField = new JTextField(stateService.getAzureOpenAIEndpoint());
    @Getter
    private final JTextField azureOpenAIDeploymentField = new JTextField(stateService.getAzureOpenAIDeployment());
    @Getter
    private final JPasswordField azureOpenAIKeyField = new JPasswordField(stateService.getAzureOpenAIKey());
    @Getter
    private final JCheckBox streamModeCheckBox = new JCheckBox("", stateService.getStreamMode());

    @Getter
    private final JCheckBox enableAzureOpenAICheckBox = new JCheckBox("", stateService.getShowAzureOpenAIFields());

    private final java.util.List<JComponent> azureComponents = new ArrayList<>();

    public LLMProvidersComponent() {
        addListeners();
    }

    @Override
    public JPanel createPanel() {
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5);

               addSettingRow(panel, gbc, "Enable Stream Mode (Beta)", streamModeCheckBox);

        addSettingRow(panel, gbc, "Enable Azure OpenAI Provider", enableAzureOpenAICheckBox);

        addAzureComponentsSettingRow(panel, gbc, "Azure OpenAI Endpoint",
                createTextWithLinkButton(azureOpenAIEndpointField, "https://learn.microsoft.com/en-us/azure/ai-services/openai/overview"));
        addAzureComponentsSettingRow(panel, gbc, "Azure OpenAI Deployment",
                createTextWithLinkButton(azureOpenAIDeploymentField, "https://learn.microsoft.com/en-us/azure/ai-services/openai/overview"));
        addAzureComponentsSettingRow(panel, gbc, "Azure OpenAI API Key",
                createTextWithPasswordButton(azureOpenAIKeyField, "https://learn.microsoft.com/en-us/azure/ai-services/openai/overview"));

        // Set initial visibility
        boolean azureEnabled = enableAzureOpenAICheckBox.isSelected();
        for (JComponent comp : azureComponents) {
            comp.setVisible(azureEnabled);
        }

        addSection(panel, gbc, "Plugin version");
        addSettingRow(panel, gbc, "v" + projectVersion.getText(), createTextWithLinkButton(new JLabel("View on GitHub"), "https://github.axa.com/samir-kerroumi/AxaAiJetbrainslugin"));

        return panel;
    }

    private void updateUrlFieldState(@NotNull JCheckBox checkbox,
                                     @NotNull JComponent urlComponent) {
        urlComponent.setEnabled(checkbox.isSelected());
    }

    @Override
    public void addListeners() {
        // Keep existing listeners
        enableAzureOpenAICheckBox.addItemListener(event -> {
            azureComponents.forEach(comp -> comp.setVisible(event.getStateChange() == ItemEvent.SELECTED));
            panel.revalidate();
            panel.repaint();
        });

        enableAzureOpenAICheckBox.addItemListener(e -> updateUrlFieldState(enableAzureOpenAICheckBox, azureOpenAIEndpointField));
    }

//    // In LLMProvidersComponent.java
//    private boolean isAzureConfigValid() {
//        return !azureOpenAIKeyField.getPassword().toString().isEmpty()
//                && !azureOpenAIEndpointField.getText().trim().isEmpty()
//                && !azureOpenAIDeploymentField.getText().trim().isEmpty();
//    }

    private void addAzureComponentsSettingRow(@NotNull JPanel panel, @NotNull GridBagConstraints gbc, String label, JComponent component) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.insets = JBUI.insets(5, 20, 5, 5); // Indent by 20 pixels on the left
        JLabel jLabel = new JLabel(label);
        panel.add(jLabel, gbc);
        azureComponents.add(jLabel);

        gbc.gridx = 1;
        panel.add(component, gbc);
        azureComponents.add(component);
        gbc.gridy++;

        gbc.insets = JBUI.insets(5);
    }
}
