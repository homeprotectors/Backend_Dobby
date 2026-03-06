package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.dto.bill.*;
import com.homeprotectors.backend.entity.BillHistory;
import com.homeprotectors.backend.service.BillHistoryService;
import com.homeprotectors.backend.service.UserContextService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bills/histories")
@RequiredArgsConstructor
public class BillHistoryController {

    private final BillHistoryService billHistoryService;
    private final UserContextService userContextService;

    @ModelAttribute
    public void loadCurrentUser(@RequestAttribute("currentUserId") UUID currentUserId) {
        userContextService.requireInternalUserId(currentUserId);
    }

    @Operation(summary = "변동 납부액 입력", description = "Create a new bill history for a given month")
    @PostMapping
    public ResponseEntity<ResponseDTO<BillHistoryCreateResponse>> createBillHistory(
            @Valid @RequestBody BillHistoryCreateRequest request,
            @RequestAttribute("currentUserId") UUID currentUserId) {
        try {
            BillHistory h = billHistoryService.create(request, currentUserId);

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
            @Valid @RequestBody BillHistoryEditRequest request,
            @RequestAttribute("currentUserId") UUID currentUserId) {

        BillHistory h = billHistoryService.update(historyId, request, currentUserId);

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
    public ResponseEntity<Void> deleteBillHistory(
            @PathVariable Long historyId,
            @RequestAttribute("currentUserId") UUID currentUserId) {
        billHistoryService.delete(historyId, currentUserId);
        return ResponseEntity.noContent().build(); // 204
    }
}
