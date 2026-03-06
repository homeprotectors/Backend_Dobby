package com.homeprotectors.backend.dto.chore;

import com.homeprotectors.backend.entity.RecurrenceType;
import com.homeprotectors.backend.entity.RoomCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ChoreCompleteResponse {
    private Long id;
    private UUID groupPublicId;
    private String title;
    private RoomCategory roomCategory;
    private RecurrenceType recurrenceType;
    private LocalDate nextDue; // 완료 후 새로 계산된 nextDue
}
