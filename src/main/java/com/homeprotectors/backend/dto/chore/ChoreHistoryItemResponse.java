package com.homeprotectors.backend.dto.chore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChoreHistoryItemResponse {

    private Long choreId;
    private LocalDate nextDue;
    private List<ChoreHistoryListResponse> history;

    @Data
    @AllArgsConstructor
    public static class ChoreHistoryListResponse {
        private Long id;
        private LocalDate doneDate;
        private Long doneBy;
    }

}