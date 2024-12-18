package com.axa.jetbrains.ui.util;

import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

public class SettingsDialogUtil {

    public static void showSettingsDialog(Project project) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, "DevoxxGenie");
    }
}
