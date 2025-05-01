package com.homeprotectors.backend.dto.chore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chore 생성할 때 사용자가 입력하는 데이터")
public class ChoreCreateRequest {

    @NotBlank
    private String title;

    @NotNull
    private Integer cycleDays;
    private LocalDate startDate;
    private Boolean reminderEnabled;

    private Integer reminderDays; // optional
}