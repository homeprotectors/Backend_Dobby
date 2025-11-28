package com.homeprotectors.backend.dto.bill;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BillHistoryCreateResponse {
    private Long id;
    private Long billId;
    private String month;     // "YYYY-MM"
    private Double amount;
    private String paidDate;  // "YYYY-MM-DD" or null
}
