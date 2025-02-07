package com.devoxx.genie.ui.settings.llm;

import com.devoxx.genie.service.PropertiesService;
import com.devoxx.genie.ui.settings.AbstractSettingsComponent;
import com.intellij.util.ui.JBUI;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LLMProvidersComponent extends AbstractSettingsComponent {

    @Getter
    private final JTextField projectVersion = new JTextField(PropertiesService.getInstance().getVersion());
    @Getter
    private final JTextField azureOpenAIEndpointField = new JTextField(stateService.getAzureOpenAIEndpoint());
    @Getter
    private final JTextField azureOpenAIClientIdField = new JTextField(stateService.getAzureOpenAIClientID());
    @Getter
    private final JPasswordField azureOpenAIClientSecretField = new JPasswordField(stateService.getAzureOpenAIClientSecret());
    @Getter
    private final JCheckBox streamModeCheckBox = new JCheckBox("", stateService.getStreamMode());

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

        addAzureComponentsSettingRow(panel, gbc, "Azure OpenAI Endpoint",
                createTextWithLinkButton(azureOpenAIEndpointField, "https://learn.microsoft.com/en-us/azure/ai-services/openai/overview"));
        addAzureComponentsSettingRow(panel, gbc, "Azure OpenAI Deployment",
                createTextWithLinkButton(azureOpenAIClientIdField, "https://learn.microsoft.com/en-us/azure/ai-services/openai/overview"));
        addAzureComponentsSettingRow(panel, gbc, "Azure OpenAI API Key",
                createTextWithPasswordButton(azureOpenAIClientSecretField));

        addSection(panel, gbc, "Plugin version");
        addSettingRow(panel, gbc, "v" + projectVersion.getText(), createTextWithLinkButton(new JLabel("View on GitHub"), "https://github.axa.com/samir-kerroumi/AxaAiJetbrainslugin"));

        return panel;
    }

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
