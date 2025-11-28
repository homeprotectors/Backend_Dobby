package com.homeprotectors.backend.dto.bill;

import com.homeprotectors.backend.entity.BillCategory;
import com.homeprotectors.backend.entity.BillType;
import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class BillCreateRequest {

    @NotBlank
    private String name;

    @NotNull @Min(0) // 소수점 가능
    private Double amount = 0.0;

    @NotNull
    private Boolean isVariable;

    // null 또는 1~31
    @Min(1) @Max(31)
    private Integer dueDate;

}
