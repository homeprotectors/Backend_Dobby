package com.homeprotectors.backend.dto.stock;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "stock item list response DTO")
public class StockListItemResponse {
    @Schema(description = "재고 ID")
    private Long id;

    @Schema(description = "재고 이름")
    private String name;

    @Schema(description = "재고 수량")
    private Integer unitQuantity;

    @Schema(description = "재고 단위")
    private String unit;

    @Schema(description = "단위 당 소비 주기 (일 단위)")
    private Integer unitDays;

    @Schema(description = "현재 재고 수량")
    private Integer currentQuantity;

    @Schema(description = "다음 소비 예정일")
    private LocalDate nextDue;

    @Schema(description = "미리 알림 일수 (일 단위)")
    private Integer reminderDays;
}
