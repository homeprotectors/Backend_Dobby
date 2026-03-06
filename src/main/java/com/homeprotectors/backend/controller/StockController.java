package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.dto.stock.StockCreateRequest;
import com.homeprotectors.backend.dto.stock.StockCreateResponse;
import com.homeprotectors.backend.dto.stock.StockListItemResponse;
import com.homeprotectors.backend.service.StockService;
import com.homeprotectors.backend.service.UserContextService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;
    private final UserContextService userContextService;

    @ModelAttribute
    public void loadCurrentUser(@RequestAttribute("currentUserId") UUID currentUserId) {
        userContextService.requireInternalUserId(currentUserId);
    }

    @Operation(summary = "stock 생성", description = "Create a new stock")
    @PostMapping
    public ResponseEntity<ResponseDTO<StockCreateResponse>> createStock(
            @Valid @RequestBody StockCreateRequest request,
            @RequestAttribute("currentUserId") UUID currentUserId) {
        StockCreateResponse response = stockService.createStock(request, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Stock created successfully", response));
    }

    @Operation(summary = "Stock 목록 조회", description = "Retrieve the list of stocks in the group that the user belongs to")
    @GetMapping
    public ResponseDTO<List<StockListItemResponse>> getStockList(
            @RequestAttribute("currentUserId") UUID currentUserId) {
        List<StockListItemResponse> stocks = stockService.getStockList(currentUserId);
        return new ResponseDTO<>(true, "Stock list retrieved", stocks);
    }

    @Operation(summary = "Stock 수정", description = "Edit an existing stock")
    @PutMapping("/{stockId}")
    public ResponseEntity<ResponseDTO<StockCreateResponse>> editStock(
            @PathVariable Long stockId,
            @Valid @RequestBody StockCreateRequest request,
            @RequestAttribute("currentUserId") UUID currentUserId) {
        StockCreateResponse response = stockService.editStock(stockId, request, currentUserId);

        return ResponseEntity.ok(new ResponseDTO<>(true, "Stock updated successfully", response));
    }

    @Operation(summary = "Stock 삭제", description = "Delete an existing stock")
    @DeleteMapping("/{stockId}")
    public ResponseEntity<Void> deleteStock(
            @PathVariable Long stockId,
            @RequestAttribute("currentUserId") UUID currentUserId) {
        stockService.deleteStock(stockId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
