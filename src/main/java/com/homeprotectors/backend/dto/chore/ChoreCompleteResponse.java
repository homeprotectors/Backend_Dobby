package com.homeprotectors.backend.dto.chore;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ChoreCompleteResponse {
    private Long id;
    private Long groupId;
    private String title;
    private LocalDate scheduledDate;
    private LocalDate newNextDue;
    private LocalDate newReminderDate;
    private Long doneBy;
}
