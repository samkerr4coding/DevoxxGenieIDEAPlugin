package com.axa.jetbrains.ui.listener;

import com.axa.jetbrains.ui.component.RoundBorder;
import com.intellij.ui.components.JBTextArea;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import static com.axa.jetbrains.ui.util.AxaAiColorsUtil.PROMPT_INPUT_BORDER;

public class PromptInputFocusListener extends FocusAdapter {

    private final JBTextArea promptInputArea;

    public PromptInputFocusListener(JBTextArea promptInputArea) {
        this.promptInputArea = promptInputArea;
    }

    @Override
    public void focusGained(FocusEvent e) {
        promptInputArea.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(PROMPT_INPUT_BORDER, 1, 5),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        promptInputArea.requestFocusInWindow();
    }

    @Override
    public void focusLost(FocusEvent e) {
        promptInputArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
}
