package com.axa.jetbrains.ui.settings.gitmerge;

import com.axa.jetbrains.ui.settings.AxaAiStateService;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GitMergeSettingsConfigurable implements Configurable {

    private final GitMergeSettingsComponent diffSettingsComponent;

    public GitMergeSettingsConfigurable() {
        diffSettingsComponent = new GitMergeSettingsComponent();
    }

    /**
     * Get the display name
     * @return the display name
     */
    @Nls
    @Override
    public String getDisplayName() {
        return "LLM Git Merge";
    }

    /**
     * Get the Prompt Settings component
     *
     * @return the component
     */
    @Nullable
    @Override
    public JComponent createComponent() {
        return diffSettingsComponent.createPanel();
    }

    @Override
    public boolean isModified() {
        AxaAiStateService stateService = AxaAiStateService.getInstance();
        GitDiffMode currentMode = determineCurrentMode(stateService);
        return currentMode != diffSettingsComponent.getGitDiffModeComboBox().getSelectedItem();
    }

    @Override
    public void apply() {
        AxaAiStateService stateService = AxaAiStateService.getInstance();
        GitDiffMode selectedMode = (GitDiffMode) diffSettingsComponent.getGitDiffModeComboBox().getSelectedItem();

        stateService.setUseSimpleDiff(selectedMode == GitDiffMode.SIMPLE_DIFF);
    }

    @Override
    public void reset() {
        AxaAiStateService stateService = AxaAiStateService.getInstance();
        GitDiffMode currentMode = determineCurrentMode(stateService);
        diffSettingsComponent.getGitDiffModeComboBox().setSelectedItem(currentMode);
    }

    private GitDiffMode determineCurrentMode(@NotNull AxaAiStateService stateService) {
        if (Boolean.TRUE.equals(stateService.getUseSimpleDiff())) {
            return GitDiffMode.SIMPLE_DIFF;
        }
        return GitDiffMode.DISABLED;
    }
}
