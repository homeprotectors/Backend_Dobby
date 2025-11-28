package com.homeprotectors.backend.dto.bill;

import com.homeprotectors.backend.entity.BillCategory;
import com.homeprotectors.backend.entity.BillType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BillEditRequest {

    private String name;

    @Min(0)
    private Double amount;

    private Boolean isVariable;

    @Min(1) @Max(31)
    private Integer dueDate;
}
