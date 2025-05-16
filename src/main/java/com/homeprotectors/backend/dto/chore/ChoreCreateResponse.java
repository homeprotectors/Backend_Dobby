package com.homeprotectors.backend.dto.chore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chore 생성 API의 응답 데이터")
public class ChoreCreateResponse {
    private Long id;
    private String title;
    private LocalDate startDate;
    private Integer cycleDays;
    private LocalDate NextDue;
    private Boolean reminderEnabled;
    private Integer reminderDays;
    private LocalDate reminderDate;
}
