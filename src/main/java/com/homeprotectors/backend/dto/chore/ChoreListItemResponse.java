package com.homeprotectors.backend.dto.chore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "chore item list response DTO")
public class ChoreListItemResponse {

    @Schema(description = "chore ID")
    private Long id;

    @Schema(description = "chore title")
    private String title;

    @Schema(description = "repeat cycle (day)")
    private Integer cycleDays;

    @Schema(description = "next due date")
    private LocalDate nextDue;

    @Schema(description = "enable reminder option")
    private Boolean reminderEnabled;

    @Schema(description = "days before due date to trigger reminder")
    private Integer reminderDays;

    @Schema(description = "next reminder date")
    private LocalDate reminderDate;
}
