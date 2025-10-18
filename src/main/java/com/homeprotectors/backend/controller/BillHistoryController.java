package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.dto.bill.*;
import com.homeprotectors.backend.entity.BillHistory;
import com.homeprotectors.backend.service.BillHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills/histories")
@RequiredArgsConstructor
public class BillHistoryController {

    private final BillHistoryService billHistoryService;

    @Operation(summary = "변동 납부액 입력", description = "Create a new bill history for a given month")
    @PostMapping
    public ResponseEntity<ResponseDTO<BillHistoryCreateResponse>> createBillHistory(
            @Valid @RequestBody BillHistoryCreateRequest request) {
        try {
            BillHistory h = billHistoryService.create(request);

            BillHistoryCreateResponse body = new BillHistoryCreateResponse(
                    h.getId(),
                    h.getBill().getId(),
                    h.getYearMonth().toString().substring(0, 7), // "YYYY-MM"
                    h.getAmount(),
                    h.getPaidDate() == null ? null : h.getPaidDate().toString()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseDTO<>(true, "History created successfully", body));

        } catch (IllegalStateException | DataIntegrityViolationException e) {
            // (groupId, billId, month) 중복
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseDTO<>(false, "Already exists for the month", null));
        }
    }

    @Operation(summary = "변동 납부액 수정", description = "Update an existing bill history")
    @PutMapping("/{historyId}")
    public ResponseEntity<ResponseDTO<BillHistoryCreateResponse>> updateBillHistory(
            @PathVariable Long historyId,
            @Valid @RequestBody BillHistoryEditRequest request) {

        BillHistory h = billHistoryService.update(historyId, request);

        BillHistoryCreateResponse body = new BillHistoryCreateResponse(
                h.getId(),
                h.getBill().getId(),
                h.getYearMonth().toString().substring(0, 7),
                h.getAmount(),
                h.getPaidDate() == null ? null : h.getPaidDate().toString()
        );

        return ResponseEntity.ok(new ResponseDTO<>(true, "History updated successfully", body));
    }

    @Operation(summary = "변동 납부액 삭제", description = "Delete a bill history")
    @DeleteMapping("/{historyId}")
    public ResponseEntity<Void> deleteBillHistory(@PathVariable Long historyId) {
        billHistoryService.delete(historyId);
        return ResponseEntity.noContent().build(); // 204
    }
}
