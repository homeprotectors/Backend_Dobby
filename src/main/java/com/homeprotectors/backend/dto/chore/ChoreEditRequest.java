package com.homeprotectors.backend.dto.chore;

import com.homeprotectors.backend.entity.RecurrenceType;
import com.homeprotectors.backend.entity.RoomCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chore 업데이트될 데이터")
public class ChoreEditRequest {

    @Schema(description = "Title of the chore", example = "Vacuuming")
    private String title;

    private RecurrenceType recurrenceType; // weekly, date, month 등
    private Set<String> selectedCycle;  // day: ["MON","WED"], month: ["3","6","9"], date: ["5"]
    private RoomCategory roomCategory;  // room, living, kitchen, bath, etc

}