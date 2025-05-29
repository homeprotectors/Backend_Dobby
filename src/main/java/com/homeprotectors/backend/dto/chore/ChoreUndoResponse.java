package com.homeprotectors.backend.dto.chore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Chore 완료 취소 API의 응답 데이터")
public class ChoreUndoResponse {
    private Long choreId;
    private LocalDate nextDue;
    private LocalDate reminderDate;
    private LocalDate lastDone;
}
