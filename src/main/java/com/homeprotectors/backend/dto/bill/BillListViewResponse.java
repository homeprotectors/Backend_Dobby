package com.homeprotectors.backend.dto.bill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BillListViewResponse {
    private String month;     // "YYYY-MM"
    private int totalCount;
    private int monthTotal;
    private Integer momDiff;      // 전월 대비 증감액 (없으면 null 아님, 0 가능)
    private Sections sections;

    @Data
    @AllArgsConstructor
    @Schema(name = "BillSections")
    public static class Sections {
        private List<BillListItemResponse> UTILITIES;
        private List<BillListItemResponse> FIXED;
        private List<BillListItemResponse> VARIABLE;
    }
}
