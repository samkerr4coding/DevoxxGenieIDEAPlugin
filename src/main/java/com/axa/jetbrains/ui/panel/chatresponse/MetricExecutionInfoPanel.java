package com.axa.jetbrains.ui.panel.chatresponse;

import com.axa.jetbrains.model.request.ChatMessageContext;
import com.axa.jetbrains.ui.settings.DevoxxGenieStateService;
import com.axa.jetbrains.util.DefaultLLMSettingsUtil;
import com.intellij.ui.JBColor;
import dev.langchain4j.model.output.TokenUsage;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

// MetricExecutionInfoPanel.java
public class MetricExecutionInfoPanel extends JPanel {

    private static final float METRIC_FONT_SIZE = 12f;
    private static final double MS_TO_SECONDS = 1000.0;

    public MetricExecutionInfoPanel(ChatMessageContext chatMessageContext) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setOpaque(false);

        if (shouldShowExecutionTime(chatMessageContext)) {
            add(createMetricLabel(chatMessageContext));
        }
    }

    private boolean shouldShowExecutionTime(ChatMessageContext chatMessageContext) {
        return Boolean.TRUE.equals(DevoxxGenieStateService.getInstance().getShowExecutionTime());
    }

    private JLabel createMetricLabel(ChatMessageContext chatMessageContext) {
        String metricInfo = buildMetricInfo(chatMessageContext);
        JLabel label = new JLabel(metricInfo);
        label.setForeground(JBColor.GRAY);
        label.setFont(label.getFont().deriveFont(METRIC_FONT_SIZE));
        return label;
    }

    private String buildMetricInfo(ChatMessageContext chatMessageContext) {
        String metricInfoLabel = String.format("ϟ %.2fs", chatMessageContext.getExecutionTimeMs() / MS_TO_SECONDS);
        TokenUsage tokenUsage = chatMessageContext.getTokenUsage();
        if (tokenUsage != null) {
            metricInfoLabel = buildTokenUsageLabel(tokenUsage, metricInfoLabel, chatMessageContext);
        }
        return metricInfoLabel;
    }

    private String buildTokenUsageLabel(TokenUsage tokenUsage, String metricInfoLabel, ChatMessageContext chatMessageContext) {
        String cost = "";
        if (DefaultLLMSettingsUtil.isApiKeyBasedProvider(chatMessageContext.getLanguageModel().getProvider())) {
            cost = String.format("- %.5f $", chatMessageContext.getCost());
        }

        // ... (Implementation for calculating token usage, you can move the relevant code here)

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedInputTokens = numberFormat.format(tokenUsage.inputTokenCount());
        String formattedOutputTokens = numberFormat.format(tokenUsage.outputTokenCount());

        metricInfoLabel += String.format(" - Tokens ↑ %s ↓️ %s %s", formattedInputTokens, formattedOutputTokens, cost);
        return metricInfoLabel;
    }
}
