package com.devoxx.genie.ui.panel;

import com.devoxx.genie.service.FileListManager;
import com.intellij.ide.util.gotoByName.GotoFileModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FileSelectionPanelFactory implements DumbAware {

    private static final int DOUBLE_CLICK = 2;
    private static final int DEBOUNCE_DELAY = 300; // milliseconds

    private FileSelectionPanelFactory() {
    }

    public static @NotNull JPanel createPanel(Project project, List<VirtualFile> openFiles) {
        DefaultListModel<VirtualFile> listModel = new DefaultListModel<>();
        JBList<VirtualFile> resultList = createResultList(project, listModel);
        JBTextField filterField = createFilterField(project, listModel, resultList, openFiles);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(400, 300)); // Increased height

        mainPanel.add(filterField, BorderLayout.NORTH);
        JBScrollPane scrollPane = new JBScrollPane(resultList);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Set a preferred size for the scroll pane
        scrollPane.setPreferredSize(new Dimension(380, 250));

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Populate the list model with open files
        for (VirtualFile file : openFiles) {
            listModel.addElement(file);
        }

        return mainPanel;
    }

    /**
     * Create the result list
     *
     * @param project the project reference
     * @param listModel the list model
     * @return the list
     */
    private static @NotNull JBList<VirtualFile> createResultList(Project project, DefaultListModel<VirtualFile> listModel) {
        JBList<VirtualFile> resultList = new JBList<>(listModel);
        resultList.setCellRenderer(new FileListCellRenderer(project));
        resultList.setVisibleRowCount(10); // Show 10 rows by default

        addMouseListenerToResultList(project, resultList);
        return resultList;
    }

    /**
     * Create the filter file field
     *
     * @param project    the project
     * @param listModel  the list model
     * @param resultList the result list
     * @return the filter field
     */
    private static @NotNull JBTextField createFilterField(Project project,
                                                          DefaultListModel<VirtualFile> listModel,
                                                          JBList<VirtualFile> resultList,
                                                          List<VirtualFile> openFiles) {
        JBTextField filterField = new JBTextField();
        filterField.getEmptyText().setText("Type to search for files...");

        AtomicReference<Timer> debounceTimer = new AtomicReference<>(new Timer(DEBOUNCE_DELAY, null));
        debounceTimer.get().setRepeats(false);

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                debounceSearch(project, filterField, listModel, resultList, debounceTimer, openFiles);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                debounceSearch(project, filterField, listModel, resultList, debounceTimer, openFiles);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                debounceSearch(project, filterField, listModel, resultList, debounceTimer, openFiles);
            }
        });

        return filterField;
    }

    /**
     * Debounce the search, only search after a DEBOUNCE_DELAY
     *
     * @param project       the project
     * @param filterField   the filter field
     * @param listModel     the list model
     * @param resultList    the result list
     * @param debounceTimer the debounce timer
     */
    private static void debounceSearch(Project project,
                                       JBTextField filterField,
                                       DefaultListModel<VirtualFile> listModel,
                                       JBList<VirtualFile> resultList,
                                       @NotNull AtomicReference<Timer> debounceTimer,
                                       List<VirtualFile> openFiles) {
        debounceTimer.get().stop();
        debounceTimer.set(new Timer(DEBOUNCE_DELAY, e ->
                searchFiles(project, filterField.getText(), listModel, resultList, openFiles)));
        debounceTimer.get().setRepeats(false);
        debounceTimer.get().start();
    }

    /**
     * Search for files
     *
     * @param project    the project
     * @param searchText the search text
     * @param listModel  the list model
     * @param resultList the result list
     */
    private static void searchFiles(Project project,
                                    String searchText,
                                    DefaultListModel<VirtualFile> listModel,
                                    JBList<VirtualFile> resultList,
                                    List<VirtualFile> openFiles) {

        new Task.Backgroundable(project, "Searching files", true) {
            private final List<VirtualFile> foundFiles = new ArrayList<>();

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ReadAction.run(() -> searchVirtualFiles(indicator));
            }

            private void searchVirtualFiles(@NotNull ProgressIndicator indicator) {
                if (searchOpenFiles(indicator)) return;

                // Search through project files
                if (searchProjectFiles(indicator)) return;

                foundFiles.sort(Comparator.comparing(VirtualFile::getName, String.CASE_INSENSITIVE_ORDER));
            }

            private boolean searchProjectFiles(@NotNull ProgressIndicator indicator) {
                GotoFileModel model = new GotoFileModel(project);
                String[] names = model.getNames(false);
                for (String name : names) {
                    if (indicator.isCanceled()) return true;
                    if (name.toLowerCase().contains(searchText.toLowerCase())) {
                        Object[] objects = model.getElementsByName(name, false, name);
                        for (Object obj : objects) {
                            if (obj instanceof PsiFile psiFile) {
                                VirtualFile virtualFile = psiFile.getVirtualFile();
                                if (virtualFile != null && !foundFiles.contains(virtualFile)) {
                                    foundFiles.add(virtualFile);
                                }
                            }
                        }
                    }
                }
                return false;
            }

            private boolean searchOpenFiles(@NotNull ProgressIndicator indicator) {
                // Search through open files
                for (VirtualFile file : openFiles) {
                    if (indicator.isCanceled()) return true;
                    if (file.getName().toLowerCase().contains(searchText.toLowerCase())) {
                        foundFiles.add(file);
                    }
                }
                return false;
            }

            @Override
            public void onSuccess() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    listModel.clear();
                    for (VirtualFile file : foundFiles) {
                        listModel.addElement(file);
                    }
                    resultList.updateUI();
                });
            }
        }.queue();
    }

    /**
     * Add a mouse listener to the result list
     *
     * @param project the project
     * @param resultList the result list
     */
    private static void addMouseListenerToResultList(Project project, @NotNull JBList<VirtualFile> resultList) {
        resultList.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == DOUBLE_CLICK) {
                addSelectedFile(project, resultList);
            }
            }
        });
    }

    /**
     * Add the selected file to the file list
     *
     * @param project the project
     * @param resultList the result list
     */
    private static void addSelectedFile(Project project, @NotNull JBList<VirtualFile> resultList) {
        VirtualFile selectedFile = resultList.getSelectedValue();
        if (selectedFile != null) {
            FileListManager fileListManager = FileListManager.getInstance();

            // Check if the file is already added
            if (!fileListManager.contains(project, selectedFile)) {
                // Add the file only if it is not already added
                fileListManager.addFile(project, selectedFile);
            }
        }
    }
}
