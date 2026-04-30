package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.stock.StockCreateRequest;
import com.homeprotectors.backend.dto.stock.StockCreateResponse;
import com.homeprotectors.backend.dto.stock.StockEditRequest;
import com.homeprotectors.backend.dto.stock.StockListItemResponse;
import com.homeprotectors.backend.entity.Stock;
import com.homeprotectors.backend.repository.GroupRepository;
import com.homeprotectors.backend.repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final GroupRepository groupRepository;
    private final UserContextService userContextService;

    public StockCreateResponse createStock(@Valid StockCreateRequest request, UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
        Long userId = userContextService.requireInternalUserId(currentUserId);

        Stock stock = new Stock();
        stock.setGroupId(groupId);
        stock.setCreatedBy(userId);
        stock.setName(request.getName());
        stock.setUnitQuantity(request.getUnitQuantity());
        stock.setUnitDays(request.getUnitDays());
        stock.setUpdatedQuantity(request.getUpdatedQuantity());

        if (request.getUpdatedQuantity() != null) {
            stock.setUpdatedQuantityDate(LocalDate.now());
        }

        stock.setCreatedAt(LocalDateTime.now());
        stockRepository.save(stock);

        LocalDate today = LocalDate.now();
        int currentQuantity = calculateCurrentQuantity(stock, today);
        int remainingDays = calculateRemainingDays(stock, currentQuantity);

        return new StockCreateResponse(
                stock.getId(),
                stock.getName(),
                stock.getUnitQuantity(),
                stock.getUnitDays(),
                currentQuantity,
                remainingDays
        );
    }

    public List<StockListItemResponse> getStockList(UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
        List<Stock> stocks = stockRepository.findByGroupId(groupId);

        LocalDate today = LocalDate.now();
        return stocks.stream()
                .map(stock -> {
                    int currentQuantity = calculateCurrentQuantity(stock, today);
                    int remainingDays = calculateRemainingDays(stock, currentQuantity);

                    return new StockListItemResponse(
                            stock.getId(),
                            stock.getName(),
                            stock.getUnitQuantity(),
                            stock.getUnitDays(),
                            currentQuantity,
                            remainingDays
                    );
                })
                .collect(Collectors.toList());
    }

    public StockCreateResponse editStock(Long stockId, StockEditRequest request, UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
        Stock stock = stockRepository.findByIdAndGroupId(stockId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));

        if (request.getName() != null) {
            stock.setName(request.getName());
        }
        if (request.getUnitQuantity() != null) {
            stock.setUnitQuantity(request.getUnitQuantity());
        }
        if (request.getUnitDays() != null) {
            stock.setUnitDays(request.getUnitDays());
        }
        if (request.getUpdatedQuantity() != null) {
            stock.setUpdatedQuantity(request.getUpdatedQuantity());
            stock.setUpdatedQuantityDate(LocalDate.now());
        }

        stockRepository.save(stock);

        LocalDate today = LocalDate.now();
        int currentQuantity = calculateCurrentQuantity(stock, today);
        int remainingDays = calculateRemainingDays(stock, currentQuantity);

        return new StockCreateResponse(
                stock.getId(),
                stock.getName(),
                stock.getUnitQuantity(),
                stock.getUnitDays(),
                currentQuantity,
                remainingDays
        );
    }

    public void deleteStock(Long stockId, UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
        Stock stock = stockRepository.findByIdAndGroupId(stockId, groupId)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found"));
        stockRepository.delete(stock);
    }

    private int calculateCurrentQuantity(Stock stock, LocalDate today) {
        if (stock.getUpdatedQuantityDate() == null || stock.getUpdatedQuantity() == null) {
            return 0;
        }

        LocalDate updatedQuantityDate = stock.getUpdatedQuantityDate();
        long daysSinceUpdate = today.toEpochDay() - updatedQuantityDate.toEpochDay();
        double dailyConsumption = (double) stock.getUnitQuantity() / stock.getUnitDays();

        double currentQuantityDouble = stock.getUpdatedQuantity() - (daysSinceUpdate * dailyConsumption);
        return Math.max(0, (int) Math.ceil(currentQuantityDouble));
    }

    private int calculateRemainingDays(Stock stock, int currentQuantity) {
        double dailyConsumption = (double) stock.getUnitQuantity() / stock.getUnitDays();
        return Math.max(0, (int) Math.round(currentQuantity / dailyConsumption));
    }
}
