package com.homeprotectors.backend.dto.chore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chore 완료 취소할 때 전달하는 데이터")
public class ChoreUndoRequest {

    @NotNull(message = "choreId는 필수입니다.")
    private Long choreId;

    @NotNull(message = "doneDate는 필수입니다.")
    @PastOrPresent(message = "미래 날짜는 허용되지 않습니다.")
    private LocalDate doneDate;
}
