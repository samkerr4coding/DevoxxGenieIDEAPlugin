package com.axa.jetbrains.ui.panel;

import com.axa.jetbrains.ui.component.RoundBorder;
import com.intellij.ui.components.JBPanel;

import javax.swing.*;
import java.awt.*;

import static com.axa.jetbrains.ui.util.AxaAiColorsUtil.GRAY_COLOR;

public class BackgroundPanel extends JBPanel<BackgroundPanel> {

    /**
     * The background panel.
     *
     * @param name the name of the panel
     */
    public BackgroundPanel(String name) {
        super.setName(name);
        setName(name);
        setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(GRAY_COLOR, 1, 5),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2d.dispose();
    }
}
