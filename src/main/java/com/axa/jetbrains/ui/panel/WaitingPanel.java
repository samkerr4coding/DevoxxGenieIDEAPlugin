package com.axa.jetbrains.ui.panel;

import com.axa.jetbrains.ui.util.WorkingMessageUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;

import java.awt.*;

import static com.axa.jetbrains.ui.util.AxaAiColorsUtil.GRAY_COLOR;
import static com.axa.jetbrains.ui.util.AxaAiColorsUtil.PROMPT_BG_COLOR;

public class WaitingPanel extends JBPanel<WaitingPanel> {

    /**
     * Create a waiting panel using a random working message.
     */
    public WaitingPanel() {
        super(new BorderLayout());
        andTransparent();
        withBackground(PROMPT_BG_COLOR);
        withMaximumSize(500, 30);
        getInsets().set(5, 5, 5, 5);

        JBLabel workingLabel = new JBLabel(WorkingMessageUtil.getWorkingMessage());
        workingLabel.setFont(workingLabel.getFont().deriveFont(12f));
        workingLabel.setForeground(GRAY_COLOR);
        workingLabel.setMaximumSize(new Dimension(500, 30));
        add(workingLabel, BorderLayout.SOUTH);
    }

    public void hideMsg() {
        setVisible(false);
    }

    public void showMsg() {
        setVisible(true);
    }
}
