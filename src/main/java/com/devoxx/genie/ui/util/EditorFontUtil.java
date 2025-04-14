package com.devoxx.genie.ui.util;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static com.devoxx.genie.ui.util.DevoxxGenieFontsUtil.SOURCE_CODE_PRO_FONT;

/**
 * Utility class for getting editor font information.
 */
public class EditorFontUtil {

    private static final int DEFAULT_FONT_SIZE = 12;

    /**
     * Gets the editor font size, falling back to a default if the EditorColorsManager is not available.
     *
     * @return the editor font size
     */
    public static int getEditorFontSize() {
        EditorColorsManager manager = EditorColorsManager.getInstance();
        if (manager != null && manager.getGlobalScheme() != null) {
            return manager.getGlobalScheme().getEditorFontSize();
        }
        return DEFAULT_FONT_SIZE;
    }

    /**
     * Creates an editor font with the appropriate size, falling back to a default if necessary.
     *
     * @return the editor font
     */
    public static @NotNull Font createEditorFont() {
        return new Font(SOURCE_CODE_PRO_FONT, Font.PLAIN, getEditorFontSize());
    }
}
