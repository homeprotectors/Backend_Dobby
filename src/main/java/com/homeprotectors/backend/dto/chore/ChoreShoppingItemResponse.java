// File: src/main/java/com/homeprotectors/backend/dto/chore/StockShoppingItemResponse.java
package com.homeprotectors.backend.dto.chore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChoreShoppingItemResponse {
    private Long id;
    private String name;
    private Integer currentQuantity;
    private Integer remainingDays;
}