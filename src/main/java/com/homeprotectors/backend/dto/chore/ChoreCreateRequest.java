package com.homeprotectors.backend.dto.chore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chore 생성할 때 사용자가 입력하는 데이터")
public class ChoreCreateRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max=20, message="10글자 이하로 입력해주세요.")
    private String title;

    @NotNull(message = "반복 주기를 입력해주세요.")
    @Min(value = 1, message = "반복 주기는 1일 이상이어야 합니다.")
    @Max(value = 365, message = "반복 주기는 365일 이하여야 합니다.")
    private Integer cycleDays;

    @FutureOrPresent(message = "시작일은 오늘 이후여야 합니다.")
    private LocalDate startDate;

    @Min(value = 0, message = "미리 알림 일수는 0일 이상 입력해주세요.")
    private Integer reminderDays; // optional
}