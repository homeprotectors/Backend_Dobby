package com.homeprotectors.backend.dto.chore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChoreHistoryItemResponse {

    private Long id;
    private LocalDate scheduledDate;
    private LocalDate doneDate;
    private Long doneBy;
}
