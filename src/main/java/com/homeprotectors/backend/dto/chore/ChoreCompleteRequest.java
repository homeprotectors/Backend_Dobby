package com.homeprotectors.backend.dto.chore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chore 완료할 때 전달하는 데이터")
public class ChoreCompleteRequest {

    @NotNull
    private Long choreId;

    @NotNull
    @Schema(description = "완료 날짜. Main View에서는 현재 날짜, Detail View에서는 사용자가 선택한 날짜 입력")
    private LocalDate doneDate;
}
