package com.homeprotectors.backend.dto.bill;

import com.homeprotectors.backend.entity.BillCategory;
import com.homeprotectors.backend.entity.BillType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BillEditRequest {

    private String name;

    private BillCategory category;

    private BillType type;

    @Min(0)
    private Integer amount;

    private Boolean isVariable;

    @Min(1) @Max(31)
    private Integer dueDate;
}
