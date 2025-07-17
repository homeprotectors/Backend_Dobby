package com.homeprotectors.backend.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stock 생성할 때 사용자가 입력하는 데이터")
public class StockCreateRequest {

    @Schema(description = "재고 이름", example = "생수")
    private String name;

    @Schema(description = "재고 수량", example = "8")
    private Integer unitQuantity;

    @Schema(description = "재고 단위", example = "병")
    private String unit;

    @Schema(description = "단위 당 소비 주기 (일 단위)", example = "7")
    private Integer unitDays;

    @Schema(description = "미리 알림 일수 (일 단위)", example = "2")
    private Integer reminderDays;

    @Schema(description = "재고", example = "3")
    private Integer currentQuantity;

}
