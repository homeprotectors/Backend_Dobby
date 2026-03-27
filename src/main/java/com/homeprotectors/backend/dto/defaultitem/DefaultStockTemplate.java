package com.homeprotectors.backend.dto.defaultitem;

public record DefaultStockTemplate(
        String name,
        Integer unitQuantity,
        Integer unitDays,
        Integer updatedQuantity
) {
}
