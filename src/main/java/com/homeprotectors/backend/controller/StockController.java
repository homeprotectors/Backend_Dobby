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
        Stock stock = stockService.createStock(request);

        StockCreateResponse response = new StockCreateResponse(
                stock.getId(),
                stock.getName(),
                stock.getUnitQuantity(),
                stock.getUnit(),
                stock.getUnitDays(),
                stock.getCurrentQuantity(),
                stock.getNextDue(),
                stock.getReminderDays()
        );

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
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<StockCreateResponse>> editStock(
            @PathVariable Long id,
            @Valid @RequestBody StockCreateRequest request) {

        Stock updated = stockService.editStock(id, request);

        StockCreateResponse response = new StockCreateResponse(
                updated.getId(),
                updated.getName(),
                updated.getUnitQuantity(),
                updated.getUnit(),
                updated.getUnitDays(),
                updated.getCurrentQuantity(),
                updated.getNextDue(),
                updated.getReminderDays()
        );

        return ResponseEntity.ok(new ResponseDTO<>(true, "Stock updated successfully", response));
    }

}
