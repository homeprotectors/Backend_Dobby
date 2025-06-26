package com.homeprotectors.backend.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stock 생성 API의 응답 데이터")
public class StockCreateResponse {
    @Schema(description = "재고 ID")
    private Long id;

    @Schema(description = "재고 이름", example = "생수")
    private String name;

    @Schema(description = "재고 수량", example = "8")
    private Integer quantity;

    @Schema(description = "재고 단위", example = "병")
    private String unit;

    @Schema(description = "소비 주기 (일 단위)", example = "7")
    private Integer estimatedConsumptionDays;

    @Schema(description = "다음 소비 예정일")
    private LocalDate nextConsumptionDate;

    @Schema(description = "미리 알림 일수 (일 단위)", example = "2")
    private Integer reminderDays;

    @Schema(description = "미리 알림 날짜")
    private LocalDate reminderDate;
}
