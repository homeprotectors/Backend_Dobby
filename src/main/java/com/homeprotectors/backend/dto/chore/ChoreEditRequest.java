package com.homeprotectors.backend.dto.chore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chore 업데이트될 데이터")
public class ChoreEditRequest {

    @Schema(description = "Title of the chore", example = "Vacuuming")
    private String title;

    @Schema(description = "Cycle in days", example = "7")
    @Min(value = 1, message = "반복 주기는 1일 이상이어야 합니다.")
    @Max(value = 365, message = "반복 주기는 365일 이하여야 합니다.")
    private Integer cycleDays;

    @Schema(description = "Number of days before due date to trigger reminder", example = "1")
    @Min(value = 0, message = "미리 알림 일수는 0일 이상 입력해주세요.")
    private Integer reminderDays;

}