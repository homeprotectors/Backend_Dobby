package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.bill.*;
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

    @Operation(summary="bill view 조회", description="Retrieve bills for a specific month")
    @GetMapping
    public BillListViewResponse getBillListView(@RequestParam("month") String month) {
        return billService.getListView(month);
    }

    @Operation(summary="bill 생성", description="Create a new bill")
    @PostMapping
    public ResponseEntity<ResponseDTO<BillCreateResponse>> createBill(@Valid @RequestBody BillCreateRequest request) {
        Bill bill = billService.createBill(request);

        BillCreateResponse response = new BillCreateResponse(
                bill.getId(),
                bill.getName(),
                bill.getAmount(),
                bill.getDueDate(),
                bill.getIsVariable()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Bill created successfully", response));
    }

    @Operation(summary = "bill 수정", description = "Update an existing bill")
    @PutMapping("/{billId}")
    public ResponseEntity<ResponseDTO<BillCreateResponse>> editBill(
            @PathVariable Long billId,
            @Valid @RequestBody BillEditRequest request) {

        Bill updated = billService.updateBill(billId, request);

        BillCreateResponse response = new BillCreateResponse(
                updated.getId(),
                updated.getName(),
                updated.getAmount(),
                updated.getDueDate(),
                updated.getIsVariable()
        );

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Bill updated successfully", response)
        );
    }

    @Operation(summary = "bill 삭제", description = "Soft delete a bill")
    @DeleteMapping("/{billId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBill(@PathVariable Long billId) {
        billService.softDeleteBill(billId);
    }

}
