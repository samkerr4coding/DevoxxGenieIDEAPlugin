package com.devoxx.genie.service.mcp;

import com.devoxx.genie.ui.settings.DevoxxGenieStateService;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for working with MCP servers
 */
@Slf4j
public class MCPService {

    /**
     * Check if MCP is enabled in the settings
     * 
     * @return true if MCP is enabled, false otherwise
     */
    public static boolean isMCPEnabled() {
        return DevoxxGenieStateService.getInstance().getMcpEnabled();
    }

    /**
     * Check if MCP debug logs are enabled
     * 
     * @return true if debug logs are enabled, false otherwise
     */
    public static boolean isDebugLogsEnabled() {
        DevoxxGenieStateService stateService = DevoxxGenieStateService.getInstance();
        return isMCPEnabled() && stateService.getMcpDebugLogsEnabled();
    }
    
    /**
     * Show the MCP log panel tool window
     * 
     * @param project The current project
     */
    public static void showMCPLogPanel(com.intellij.openapi.project.Project project) {
        if (isDebugLogsEnabled() && project != null) {
            com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                com.intellij.openapi.wm.ToolWindow toolWindow = 
                    com.intellij.openapi.wm.ToolWindowManager.getInstance(project).getToolWindow("DevoxxGenieMCPLogs");
                if (toolWindow != null && !toolWindow.isVisible()) {
                    toolWindow.show();
                }
            });
        }
    }
    
    /**
     * Log a debug message if debug logs are enabled
     * 
     * @param message The message to log
     */
    public static void logDebug(String message) {
        if (isDebugLogsEnabled()) {
            log.info("[MCP Debug] {}", message);
        }
    }
}
