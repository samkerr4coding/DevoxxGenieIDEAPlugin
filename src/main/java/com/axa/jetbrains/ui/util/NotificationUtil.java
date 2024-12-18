package com.axa.jetbrains.ui.util;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;

public class NotificationUtil {

    private NotificationUtil() {
    }

    public static void sendNotification(Project project, String content) {
        NotificationGroup notificationGroup =
            NotificationGroupManager.getInstance().getNotificationGroup("com.axa.jetbrains.notifications");
        Notification notification = notificationGroup.createNotification(content, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification, project);
    }
}
