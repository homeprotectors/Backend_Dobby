package com.homeprotectors.backend.dto.defaultitem;

import com.homeprotectors.backend.entity.RecurrenceType;
import com.homeprotectors.backend.entity.RoomCategory;

import java.util.Set;

public record DefaultChoreTemplate(
        String title,
        RecurrenceType recurrenceType,
        Set<String> selectedCycle,
        RoomCategory roomCategory
) {
}
