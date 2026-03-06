package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.chore.ChoreListItemResponse;
import com.homeprotectors.backend.dto.stock.StockCreateRequest;
import com.homeprotectors.backend.dto.stock.StockCreateResponse;
import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.dto.stock.StockListItemResponse;
import com.homeprotectors.backend.entity.Stock;
import com.homeprotectors.backend.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;
    
    @Operation(summary = "stock 생성", description = "Create a new stock")
    @PostMapping
    public ResponseEntity<ResponseDTO<StockCreateResponse>> createStock(@Valid @RequestBody StockCreateRequest request) {
        StockCreateResponse response = stockService.createStock(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Stock created successfully", response));
    }

    @Operation(summary = "Stock 목록 조회", description = "Retrieve the list of stocks in the group that the user belongs to")
    @GetMapping
    public ResponseDTO<List<StockListItemResponse>> getStockList() {
        List<StockListItemResponse> stocks = stockService.getStockList();  // 인증 기반 그룹 필터링 가정
        return new ResponseDTO<>(true, "Chore list retrieved", stocks);
    }

    @Operation(summary = "Stock 수정", description = "Edit an existing stock")
    @PutMapping("/{stockId}")
    public ResponseEntity<ResponseDTO<StockCreateResponse>> editStock(
            @PathVariable Long stockId,
            @Valid @RequestBody StockCreateRequest request) {
        StockCreateResponse response = stockService.editStock(stockId, request);

        return ResponseEntity.ok(new ResponseDTO<>(true, "Stock updated successfully", response));
    }

    @Operation(summary = "Stock 삭제", description = "Delete an existing stock")
    @DeleteMapping("/{stockId}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long stockId) {
        stockService.deleteStock(stockId);
        return ResponseEntity.noContent().build();
    }

}
