package com.homeprotectors.backend.dto.bill;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bill 생성 API의 응답 데이터")
public class BillCreateResponse {
    @Schema(description = "청구서 ID")
    private Long billId;

    @Schema(description = "청구서 제목")
    private String name;

    @Schema(description = "청구서 금액")
    private Double amount;

    @Schema(description = "매월 청구 일자")
    private Integer dueDate;

    @Schema(description = "변동 청구서 여부", example = "false")
    private Boolean isVariable; // 변동 청구서인지 여부

}
