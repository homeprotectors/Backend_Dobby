package com.homeprotectors.backend.dto.bill;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BillHistoryEditRequest {

    @NotNull
    private Long billId;

    // "YYYY-MM" 형식 강제
    @NotNull
    @Pattern(regexp = "^\\d{4}-\\d{2}$")
    private String month;

    // 수정 시 납부액(선택적)
    @Min(0)
    private Double amount;
}
