package com.homeprotectors.backend.dto.defaultitem;

import java.util.List;

public record DefaultItemTemplate(
        int version,
        List<DefaultChoreTemplate> chores,
        List<DefaultStockTemplate> stocks
) {
}
