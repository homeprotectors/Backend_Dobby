package com.homeprotectors.backend.dto.chore;

import com.homeprotectors.backend.entity.RecurrenceType;
import com.homeprotectors.backend.entity.RoomCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chore 생성 API의 응답 데이터")
public class ChoreCreateResponse {
    private Long id;
    private String title;
    private RecurrenceType recurrenceType; // weekly, date, month 등
    private Set<String> selectedCycle;  // day: ["MON","WED"], month: ["3","6","9"], date: ["5"]
    private RoomCategory roomCategory;
    private LocalDate nextDue;
}
