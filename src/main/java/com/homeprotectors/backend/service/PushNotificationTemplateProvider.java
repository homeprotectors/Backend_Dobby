package com.homeprotectors.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationTemplateProvider {

    private final String dailyChoreReminderTitle;
    private final String dailyChoreReminderBodyTemplate;

    public PushNotificationTemplateProvider(
            @Value("${app.push.templates.daily-chore-reminder.title}") String dailyChoreReminderTitle,
            @Value("${app.push.templates.daily-chore-reminder.body}") String dailyChoreReminderBodyTemplate
    ) {
        this.dailyChoreReminderTitle = dailyChoreReminderTitle;
        this.dailyChoreReminderBodyTemplate = dailyChoreReminderBodyTemplate;
    }

    public String getDailyChoreReminderTitle() {
        return dailyChoreReminderTitle;
    }

    public String renderDailyChoreReminderBody(long choreCount) {
        return dailyChoreReminderBodyTemplate.replace("{choreCount}", String.valueOf(choreCount));
    }
}
