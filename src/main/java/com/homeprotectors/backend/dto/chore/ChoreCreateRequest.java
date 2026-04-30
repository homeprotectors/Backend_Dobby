package com.homeprotectors.backend.dto.chore;

import com.homeprotectors.backend.entity.RecurrenceType;
import com.homeprotectors.backend.entity.RoomCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chore creation request")
public class ChoreCreateRequest {

    @NotBlank(message = "title is required")
    @Size(max = 20, message = "title must be 20 characters or fewer")
    private String title;

    @NotNull(message = "recurrenceType is required")
    private RecurrenceType recurrenceType;

    @NotNull(message = "selectedCycle is required")
    @NotEmpty(message = "selectedCycle must not be empty")
    private Set<String> selectedCycle;

    @NotNull(message = "roomCategory is required")
    private RoomCategory roomCategory;
}
