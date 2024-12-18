package com.axa.jetbrains.ui.settings;

import javax.swing.*;

public interface SettingsComponent {

    /**
     * Create the panel
     */
    JPanel createPanel();

    /**
     * Adds listeners to the panel components
     */
    void addListeners();
}
