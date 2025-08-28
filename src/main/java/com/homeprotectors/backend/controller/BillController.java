package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.bill.BillCreateRequest;
import com.homeprotectors.backend.dto.bill.BillCreateResponse;
import com.homeprotectors.backend.dto.bill.BillListItemResponse;
import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.entity.Bill;
import com.homeprotectors.backend.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    @Operation(summary="bill 생성", description="Create a new bill")
    @PostMapping
    public ResponseEntity<ResponseDTO<BillCreateResponse>> createBill(@Valid @RequestBody BillCreateRequest request) {
        Bill bill = billService.createBill(request);

        BillCreateResponse response = new BillCreateResponse(
                bill.getId(),
                bill.getName(),
                bill.getAmount(),
                bill.getDueDate(),
                bill.getIsVariable(),
                bill.getReminderDays()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Bill created successfully", response));
    }

    @Operation(summary="bill 목록 조회", description="Retrieve the list of bills in the group that the user belongs to")
    @GetMapping
    public ResponseDTO<List<BillListItemResponse>> getBillList() {
        List<BillListItemResponse> bills = billService.getbillList();  // 인증 기반 그룹 필터링 가정
        return new ResponseDTO<>(true, "Bill list retrieved", bills);
    }
}
