package com.homeprotectors.backend.dto.bill;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BillListItemResponse {
    private Long id;
    private String name;
    private Integer dueDate; // null 가능
    private Integer amount;  // 화면에 표시할 그 달 기준 금액
}
