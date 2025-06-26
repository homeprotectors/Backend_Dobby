package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.stock.StockCreateRequest;
import com.homeprotectors.backend.dto.stock.StockCreateResponse;
import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.entity.Stock;
import com.homeprotectors.backend.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                stock.getQuantity(  ),
                stock.getUnit(),
                stock.getEstimatedConsumptionDays(),
                stock.getNextDue(),
                stock.getReminderDays(),
                stock.getReminderDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Stock created successfully", response));
    }
}
