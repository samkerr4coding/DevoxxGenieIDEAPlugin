package com.axa.jetbrains.action;

import com.axa.jetbrains.controller.listener.TokenCalculationListener;
import com.axa.jetbrains.model.enumarations.ModelProvider;
import com.axa.jetbrains.service.TokenCalculationService;
import com.axa.jetbrains.ui.settings.AxaAiStateService;
import com.axa.jetbrains.ui.util.NotificationUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class CalcTokensForDirectoryAction extends DumbAwareAction implements TokenCalculationListener {
    private Project project;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.project = e.getProject();
        VirtualFile selectedDir = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (project == null || selectedDir == null || !selectedDir.isDirectory()) {
            return;
        }

        AxaAiStateService stateService = AxaAiStateService.getInstance();
        ModelProvider selectedProvider = ModelProvider.fromString(stateService.getSelectedProvider(project.getLocationHash()));

        int maxTokens = stateService.getDefaultWindowContext();

        new TokenCalculationService()
            .calculateTokensAndCost(project, selectedDir, maxTokens, selectedProvider, null, false, this);
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        e.getPresentation().setEnabledAndVisible(file != null && file.isDirectory());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    @Override
    public void onTokenCalculationComplete(String message) {
        NotificationUtil.sendNotification(project, message);
    }
}
