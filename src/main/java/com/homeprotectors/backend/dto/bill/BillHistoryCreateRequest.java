package com.homeprotectors.backend.dto.bill;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BillHistoryCreateRequest {

    @NotNull
    private Long billId;

    // "YYYY-MM" 형식 강제
    @NotNull
    @Pattern(regexp = "^\\d{4}-\\d{2}$")
    private String month;

    @NotNull @Min(0)
    private Integer amount;

    // 선택값: "YYYY-MM-DD" or null
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
    private String paidDate;
}
