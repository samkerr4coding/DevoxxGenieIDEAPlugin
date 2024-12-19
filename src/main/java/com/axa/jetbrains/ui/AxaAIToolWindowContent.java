package com.axa.jetbrains.ui;

import com.axa.jetbrains.model.Constant;
import com.axa.jetbrains.model.LanguageModel;
import com.axa.jetbrains.model.enumarations.ModelProvider;
import com.axa.jetbrains.service.ConversationStorageService;
import com.axa.jetbrains.ui.component.border.AnimatedGlowingBorder;
import com.axa.jetbrains.ui.listener.GlowingListener;
import com.axa.jetbrains.ui.listener.SettingsChangeListener;
import com.axa.jetbrains.ui.panel.ConversationPanel;
import com.axa.jetbrains.ui.panel.LlmProviderPanel;
import com.axa.jetbrains.ui.panel.PromptOutputPanel;
import com.axa.jetbrains.ui.panel.SubmitPanel;
import com.axa.jetbrains.ui.settings.AxaAiStateService;
import com.axa.jetbrains.ui.topic.AppTopics;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.util.messages.MessageBusConnection;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import static com.axa.jetbrains.model.Constant.MESSAGES;

/**
 * The Axa AI plugin Tool Window Content.
 */
public class AxaAIToolWindowContent implements SettingsChangeListener, GlowingListener {

    private static final float SPLITTER_PROPORTION = 0.75f;
    private static final float MIN_PROPORTION = 0.3f;
    private static final float MAX_PROPORTION = 0.85f;

    @Getter
    private final Project project;
    @Getter
    private final ResourceBundle resourceBundle = ResourceBundle.getBundle(MESSAGES);
    @Getter
    private final JPanel contentPanel = new JPanel();
    @Getter
    private LlmProviderPanel llmProviderPanel;
    @Getter
    private ConversationPanel conversationPanel;
    @Getter
    private SubmitPanel submitPanel;
    @Getter
    private PromptOutputPanel promptOutputPanel;

    private boolean isInitializationComplete = false;

    @Getter
    private final ConversationStorageService storageService = ConversationStorageService.getInstance();
    private final AnimatedGlowingBorder animatedBorder;

    /**
     * The Axa AI Tool Window Content constructor.
     *
     * @param toolWindow the tool window
     */
    public AxaAIToolWindowContent(@NotNull ToolWindow toolWindow) {
        project = toolWindow.getProject();

        AxaAiStateService stateService = AxaAiStateService.getInstance();
        stateService.addLoadListener(this::onStateLoaded);
        stateService.loadState(AxaAiStateService.getInstance());

        setupMessageBusConnection(toolWindow);

        animatedBorder = new AnimatedGlowingBorder(contentPanel);
    }

    private void onStateLoaded() {

        if (!isInitializationComplete) {
            setupUI();
            isInitializationComplete = true;
        }
    }

    /**
     * Set up the UI Components: top panel and splitter.
     */
    private void setupUI() {
        initializeComponents();
        setupLayout();
        setupListeners();
    }

    private void initializeComponents() {
        llmProviderPanel = new LlmProviderPanel(project);
        promptOutputPanel = new PromptOutputPanel(project, resourceBundle);
        submitPanel = new SubmitPanel(this);
        conversationPanel = new ConversationPanel(this);
    }

    private void setupLayout() {
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(createTopPanel(), BorderLayout.NORTH);
        contentPanel.add(createSplitter(), BorderLayout.CENTER);
    }

    private void setupListeners() {
        llmProviderPanel.getModelNameComboBox().addActionListener(this::processModelNameSelection);
    }

    @Override
    public void startGlowing() {
        animatedBorder.startGlowing();
    }

    @Override
    public void stopGlowing() {
        animatedBorder.stopGlowing();
    }

    /**
     * Create the top panel.
     *
     * @return the top panel
     */
    private @NotNull JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(llmProviderPanel, BorderLayout.NORTH);
        topPanel.add(conversationPanel, BorderLayout.CENTER);
        return topPanel;
    }

    /**
     * Create the splitter.
     *
     * @return the splitter
     */
    private @NotNull Splitter createSplitter() {
        OnePixelSplitter splitter =
                new OnePixelSplitter(true, SPLITTER_PROPORTION, MIN_PROPORTION, MAX_PROPORTION);
        splitter.setFirstComponent(promptOutputPanel);
        splitter.setSecondComponent(submitPanel);
        splitter.setHonorComponentsMinimumSize(true);

        return splitter;
    }

    /**
     * Set up the message bus connection.
     *
     * @param toolWindow the tool window
     */
    private void setupMessageBusConnection(@NotNull ToolWindow toolWindow) {
        MessageBusConnection messageBusConnection = project.getMessageBus().connect();
        messageBusConnection.subscribe(AppTopics.LLM_SETTINGS_CHANGED_TOPIC, this.llmProviderPanel);
        messageBusConnection.subscribe(AppTopics.CUSTOM_PROMPT_CHANGED_TOPIC, this.promptOutputPanel);
        messageBusConnection.subscribe(AppTopics.CONVERSATION_TOPIC, this.conversationPanel);
        Disposer.register(toolWindow.getDisposable(), messageBusConnection);
    }

    /**
     * Refresh the UI elements because the settings have changed.
     */
    @Override
    public void settingsChanged(boolean hasKey) {
        ModelProvider currentProvider = (ModelProvider) llmProviderPanel.getModelProviderComboBox().getSelectedItem();
        LanguageModel currentModel = (LanguageModel) llmProviderPanel.getModelNameComboBox().getSelectedItem();

        llmProviderPanel.getModelProviderComboBox().removeAllItems();
        llmProviderPanel.getModelNameComboBox().removeAllItems();
        llmProviderPanel.addModelProvidersToComboBox();

        if (currentProvider != null) {
            llmProviderPanel.getModelProviderComboBox().setSelectedItem(currentProvider);
            llmProviderPanel.updateModelNamesComboBox(currentProvider.getName());

            if (currentModel != null) {
                llmProviderPanel.getModelNameComboBox().setSelectedItem(currentModel);
            }
        } else {
            llmProviderPanel.setLastSelectedProvider();
        }
    }

    /**
     * Process the model name selection.
     */
    private void processModelNameSelection(@NotNull ActionEvent e) {

        if (e.getActionCommand().equals(Constant.COMBO_BOX_CHANGED) && isInitializationComplete) {
            LanguageModel selectedModel = (LanguageModel) llmProviderPanel.getModelNameComboBox().getSelectedItem();
            if (selectedModel != null) {
                AxaAiStateService.getInstance().setSelectedLanguageModel(project.getLocationHash(), selectedModel.getModelName());
                submitPanel.getActionButtonsPanel().updateTokenUsage(selectedModel.getContextWindow());
            }
        }
    }
}
