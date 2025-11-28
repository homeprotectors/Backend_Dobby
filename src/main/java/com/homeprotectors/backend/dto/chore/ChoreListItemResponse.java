package com.homeprotectors.backend.dto.chore;

import com.homeprotectors.backend.entity.RecurrenceType;
import com.homeprotectors.backend.entity.RoomCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "chore item list response DTO")
public class ChoreListItemResponse {
    private Long id;
    private String title;
    private RecurrenceType recurrenceType;
    private Set<String> selectedCycle;
    private RoomCategory roomCategory;
    private LocalDate nextDue;

    private boolean shoppingContainer;
    private List<ChoreShoppingItemResponse> shoppingItems;
}
