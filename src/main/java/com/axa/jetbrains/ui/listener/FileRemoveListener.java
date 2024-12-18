package com.axa.jetbrains.ui.listener;

import com.intellij.openapi.vfs.VirtualFile;

public interface FileRemoveListener {

    void onFileRemoved(VirtualFile file);
}


