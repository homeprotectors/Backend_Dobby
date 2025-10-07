package com.homeprotectors.backend.dto.chore;

import com.homeprotectors.backend.entity.RecurrenceType;
import com.homeprotectors.backend.entity.RoomCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chore 생성할 때 사용자가 입력하는 데이터")
public class ChoreCreateRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max=20, message="10글자 이하로 입력해주세요.")
    private String title;
    private RecurrenceType recurrenceType; // weekly, date, month 등
    private Set<String> selectedCycle;  // day: ["MON","WED"], month: ["3","6","9"], date: ["5"]
    private RoomCategory roomCategory;  // room, living, kitchen, bath, etc
}