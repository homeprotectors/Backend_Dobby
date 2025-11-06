package com.homeprotectors.backend.dto.chore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class ChoreSectionsResponse {
    private Sections sections;

    @Getter @Builder
    @Schema(name = "ChoreSections")
    public static class Sections {
        private Section thisWeek;  // overdue 포함(최상단)
        private Section nextWeek;
        private Section thisMonth;
        private Section nextMonth;
    }
    @Getter @Builder
    public static class Section {
        private int count;
        private List<ChoreListItemResponse> items;
    }
}
