package com.axa.jetbrains.ui.component;

import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.axa.jetbrains.ui.util.DevoxxGenieColorsUtil.HOVER_BG_COLOR;
import static com.axa.jetbrains.ui.util.DevoxxGenieColorsUtil.TRANSPARENT_COLOR;

public class JHoverButton extends JButton {

    public JHoverButton(Icon icon, boolean tight) {
        this("", icon, tight);
    }

    public JHoverButton(@NotNull String text, Icon icon, boolean tight) {
        super(icon);

        if (!text.isBlank()) {
            setText(text);
        }

        if (tight) {
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            if (icon != null) {
                setMinimumSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
                setPreferredSize(new Dimension(icon.getIconWidth() + 8, icon.getIconHeight() + 4)); // +4 for minimal padding
            }
            setMargin(JBUI.emptyInsets());
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder());
        }

        initUI();
    }

    private void initUI() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setOpaque(true);
                setBackground(HOVER_BG_COLOR);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setOpaque(false);
                setBackground(TRANSPARENT_COLOR);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getModel().isSelected()) {
            Icon icon = getIcon();
            if (icon != null) {
                int iconWidth = icon.getIconWidth();
                int iconHeight = icon.getIconHeight();
                int x = (getWidth() - iconWidth) / 2;
                int y = (getHeight() - iconHeight) / 2;
                g.setColor(getBackground());
                g.fillRect(x, y, iconWidth, iconHeight);
            }
        }
        super.paintComponent(g);
    }
}
