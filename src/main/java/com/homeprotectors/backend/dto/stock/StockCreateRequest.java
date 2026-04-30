package com.homeprotectors.backend.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stock creation or update request")
public class StockCreateRequest {

    @NotBlank(message = "name is required")
    @Schema(description = "Stock name", example = "Dish Soap")
    private String name;

    @NotNull(message = "unitQuantity is required")
    @Min(value = 1, message = "unitQuantity must be at least 1")
    @Schema(description = "Quantity per purchase unit", example = "8")
    private Integer unitQuantity;

    @NotNull(message = "unitDays is required")
    @Min(value = 1, message = "unitDays must be at least 1")
    @Schema(description = "Expected days one unit lasts", example = "7")
    private Integer unitDays;

    @NotNull(message = "updatedQuantity is required")
    @Min(value = 0, message = "updatedQuantity must be zero or greater")
    @Schema(description = "Current quantity", example = "3")
    private Integer updatedQuantity;
}
