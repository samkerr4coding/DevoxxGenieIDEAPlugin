package com.axa.jetbrains.ui.component;

import com.intellij.ui.ColorUtil;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StyleSheetUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.html.StyleSheet;

import static com.axa.jetbrains.ui.util.AxaAiFontsUtil.SOURCE_CODE_PRO_FONT;

public class StyleSheetsFactory {

    @Contract(" -> new")
    public static @NotNull StyleSheet createCodeStyleSheet() {
        return StyleSheetUtil.loadStyleSheet(
            "code, pre, .pre { " +
                "   font-family: '" + SOURCE_CODE_PRO_FONT + "'; " +
                "   font-size: 14pt;" +
                "}"
        );
    }

    @Contract(" -> new")
    public static @NotNull StyleSheet createParagraphStyleSheet() {
        return StyleSheetUtil.loadStyleSheet(
            "h6 { font-size: " + scaleFontSize(1) + "}" +
                "h5 { font-size: " + scaleFontSize(2) + "}" +
                "h4 { font-size: " + scaleFontSize(3) + "}" +
                "h3 { font-size: " + scaleFontSize(4) + "}" +
                "h2 { font-size: " + scaleFontSize(6) + "}" +
                "h1 { font-size: " + scaleFontSize(8) + "}" +
                "h1, h2, h3, h4, h5, h6, p, ul, ol { margin: 0 0 0 0; " + paragraphSpacing() + " }" +
                "p, li { line-height: 125%; }" +
                "ul { margin-left: " + JBUIScale.scale(10) + "px; }" +
                "ol { margin-left: " + JBUIScale.scale(20) + "px; }" +
                "li { padding: " + JBUIScale.scale(1) + "px 0 " + JBUIScale.scale(2) + "px 0; }" +
                "code, pre, .pre { " +
                "   font-family: '" + SOURCE_CODE_PRO_FONT + "'; " +
                "   font-size: 14pt;" +
                "   color: orange" +
                "}" +
                "hr {" +
                " margin: " + JBUIScale.scale(4) + "px 0;" +
                " border-bottom: " + JBUIScale.scale(1) + "px solid " + ColorUtil.toHtmlColor(UIUtil.getTooltipSeparatorColor()) + ";" +
                " width: 100%;" +
                "}" +
                "a { color: " + ColorUtil.toHtmlColor(JBUI.CurrentTheme.Link.Foreground.ENABLED) + "; text-decoration: none; }"
        );
    }

    @Contract("_ -> new")
    private static @NotNull String scaleFontSize(int increment) {
        return Integer.toString(UIManager.getFont("Label.font").getSize() + increment);
    }

    private static @NotNull String paragraphSpacing() {
        return "padding: " + JBUIScale.scale(4) + "px 0 " + JBUIScale.scale(4) + "px 0";
    }
}
