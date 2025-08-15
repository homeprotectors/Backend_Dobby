package com.homeprotectors.backend.dto.bill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillCreateRequest {

    @NotNull
    @Schema(description = "청구서 제목")
    private String name; // 청구서 제목

    @NotNull
    @Schema(description = "청구서 금액")
    private Double amount; // 청구 금액

    @NotNull
    @Schema(description = "매월 청구 일자 (1-31)", example = "30")
    private Integer dueDate; // 청구서 마감일 (일 단위로 입력, 예: 30)

    @Schema(description = "변동 청구서 여부", example = "false")
    private Boolean isVariable; // 변동 청구서인지 여부

    @Schema(description = "미리 알림 일수 (일 단위)", example = "2")
    private Integer reminderDays; // 미리 알림 일수 (일 단위, 선택적)

}
