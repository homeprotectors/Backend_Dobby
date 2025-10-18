package com.homeprotectors.backend.dto.bill;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BillHistoryEditRequest {

    // 수정 시 납부액(선택적)
    @Min(0)
    private Integer amount;

    // 수정 시 납부일(선택적)
    // null 가능 — 입력한 경우 "YYYY-MM-DD" 형식 강제
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
    private String paidDate;
}
