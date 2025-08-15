package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.bill.BillCreateRequest;
import com.homeprotectors.backend.dto.bill.BillCreateResponse;
import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.entity.Bill;
import com.homeprotectors.backend.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    @Operation(summary="bill 생성", description="Create a new bill")
    @PostMapping
    public ResponseEntity<ResponseDTO<BillCreateResponse>> createBill(BillCreateRequest request) {
        Bill bill = billService.createBill(request);

        BillCreateResponse response = new BillCreateResponse(
                bill.getId(),
                bill.getName(),
                bill.getAmount(),
                bill.getDueDate(),
                bill.getIsVariable(),
                bill.getReminderDays(),
                bill.getReminderDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Bill created successfully", response));
    }
}
