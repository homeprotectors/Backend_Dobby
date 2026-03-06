package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.stock.StockCreateRequest;
import com.homeprotectors.backend.dto.stock.StockCreateResponse;
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
import java.util.stream.Collectors;

@Data
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final GroupRepository groupRepository;

    public StockCreateResponse createStock(@Valid StockCreateRequest request) {
        Stock stock = new Stock();
        stock.setGroupId(1L); // TODO: 임시 group ID
        stock.setCreatedBy(1L); // TODO: 임시 생성자 ID, 실제로는 인증된 사용자 ID로 설정해야 함
        stock.setName(request.getName());
        stock.setUnitQuantity(request.getUnitQuantity());
        stock.setUnitDays(request.getUnitDays());
        stock.setUpdatedQuantity(request.getUpdatedQuantity());

        // UpdatedQuantity가 업데이트됐을 때만 현재 시간으로 UpdatedQuantityDate 설정
        if (request.getUpdatedQuantity() != null) {
            stock.setUpdatedQuantityDate(LocalDate.now());
        }

        // 현재 시간으로 createdAt 설정
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

    public List<StockListItemResponse> getStockList() {
        Long groupId = 1L; // TODO: 인증 기반으로 동적으로 받을 예정

        List<Stock> stocks = stockRepository.findByGroupId(groupId);

        // currentQuantity 계산 로직
        // 필드가 아닌 변수로 currentQuantity를 계산해서 response로 반환

        // updatedQuantityDate로부터 현재까지의 일수를 계산
        // unitQuantity / unitDays로 하루에 소비되는 단위 수량 계산
        // 현재 예상 currentQuantity = updatedQuantity - (daysSinceUpdate * dailyConsumption)
        // crruentQuantity = updatedQuantity - (daysSinceUpdate * (unitQuantity / unitDays))
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

    public StockCreateResponse editStock(Long stockId, StockCreateRequest request) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));

        // 값이 넘어온 것들만 업데이트- 없으면 기존 값 유지
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

            // UpdatedQuantity가 업데이트됐을 때만 현재 시간으로 UpdatedQuantityDate 설정
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

    public void deleteStock(Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found"));

        // TODO: 인증 사용자 그룹 소속 여부 확인 (JWT 인증 기반으로 구현 예정)
        stockRepository.delete(stock);
    }

    private int calculateCurrentQuantity(Stock stock, LocalDate today) {
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
