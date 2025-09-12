package com.homeprotectors.backend.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "재고 수정 요청 DTO")
public class StockEditRequest {

    @Schema(description = "재고 ID")
    private String name;

    @Schema(description = "재고 수량")
    private Integer unitQuantity;

    @Schema(description = "단위 당 소비 주기 (일 단위)")
    private Integer unitDays;

    @Schema(description = "현재 재고 수량")
    private Integer updatedQuantity;
}
