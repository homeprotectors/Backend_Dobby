package com.homeprotectors.backend.dto.bill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillListItemResponse {
    private Long billId; // 청구서 ID
    private String name; // 청구서 제목
    private Double amount; // 청구서 금액
    private Integer dueDate; // 매월 청구 일자
    private Boolean isVariable; // 변동 청구서 여부
    private Integer reminderDays; // 미리 알림 일수 (일 단위)
    private LocalDate reminderDate;
}
