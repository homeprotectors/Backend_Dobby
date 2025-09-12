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
    private Integer unitQuantity;

    @Schema(description = "소비 주기 (일 단위)", example = "7")
    private Integer unitDays;

    @Schema(description = "입력 받은 재고 수량", example = "3")
    private Integer updatedQuantity;
}
