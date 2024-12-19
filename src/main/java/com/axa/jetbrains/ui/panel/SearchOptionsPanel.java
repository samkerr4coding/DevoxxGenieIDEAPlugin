package com.axa.jetbrains.ui.panel;

import com.axa.jetbrains.ui.component.InputSwitch;
import com.axa.jetbrains.ui.listener.GitDiffStateListener;
import com.axa.jetbrains.ui.settings.AxaAiStateService;
import com.axa.jetbrains.ui.topic.AppTopics;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SearchOptionsPanel extends JPanel {
    private final List<InputSwitch> switches = new ArrayList<>();
    private static final int DEFAULT_HEIGHT = JBUI.scale(30);

    public SearchOptionsPanel(Project project) {
        super(new FlowLayout(FlowLayout.LEFT, JBUI.scale(10), 0));
        setOpaque(false);

        AxaAiStateService stateService = AxaAiStateService.getInstance();

        // Create switches
        InputSwitch ragSwitch = new InputSwitch(
                "RAG",
                "Enable RAG-enabled code search"
        );

        InputSwitch gitDiffSwitch = new InputSwitch(
                "Git Diff",
                "Show Git diff window to compare and merge code suggestions"
        );

        InputSwitch webSearchSwitch = new InputSwitch(
                "Web",
                "Search the web for additional information"
        );

        // Add switches to our list for tracking
        switches.add(ragSwitch);
        switches.add(gitDiffSwitch);
        switches.add(webSearchSwitch);

        // Initialize visibility based on state service
        updateInitialVisibility(stateService);

        // Load saved states for enabled switches
        gitDiffSwitch.setSelected(stateService.getGitDiffActivated());

        // Ensure only one switch is initially active
        enforceInitialSingleSelection();

        gitDiffSwitch.addEventSelected(selected -> {
            if (selected) {
                deactivateOtherSwitches(gitDiffSwitch);
            }
            stateService.setGitDiffActivated(selected);
            updatePanelVisibility();
        });

        // Set up message bus listeners for visibility changes
        setupMessageBusListeners();

        // Add components
        add(ragSwitch);
        add(gitDiffSwitch);
        add(webSearchSwitch);

        // Add some padding
        setBorder(JBUI.Borders.empty(5, 10));

        // Update panel visibility based on initial state
        updatePanelVisibility();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, shouldBeVisible() ? DEFAULT_HEIGHT : 0);
    }

    @Override
    public Dimension getPreferredSize() {
        if (!shouldBeVisible()) {
            return new Dimension(0, 0);
        }
        Dimension size = super.getPreferredSize();
        return new Dimension(size.width, DEFAULT_HEIGHT);
    }

    private boolean shouldBeVisible() {
        return switches.stream().anyMatch(Component::isVisible);
    }

    private void updatePanelVisibility() {
        setVisible(shouldBeVisible());
        revalidate();
        repaint();
    }

    private void updateInitialVisibility(@NotNull AxaAiStateService stateService) {
        // Set initial visibility based on state service
        switches.get(1).setVisible(stateService.getGitDiffEnabled());

        // Update panel visibility
        updatePanelVisibility();
    }

    private void setupMessageBusListeners() {
        Application application = ApplicationManager.getApplication();
        MessageBusConnection connect = application.getMessageBus().connect();

        connect.subscribe(AppTopics.GITDIFF_STATE_TOPIC,
                (GitDiffStateListener) enabled -> {
                    InputSwitch gitDiffSwitch = switches.get(1);
                    gitDiffSwitch.setVisible(enabled);
                    gitDiffSwitch.setSelected(enabled);
                    updatePanelVisibility();
                });
    }

    private void deactivateOtherSwitches(InputSwitch activeSwitch) {
        switches.stream()
                .filter(sw -> sw != activeSwitch && sw.isVisible())
                .forEach(sw -> sw.setSelected(false));
    }

    private void enforceInitialSingleSelection() {
        // Find the first active and visible switch
        switches.stream()
                .filter(sw -> sw.isSelected() && sw.isVisible())
                .findFirst()
                .ifPresent(this::deactivateOtherSwitches);
    }
}