package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.chore.ChoreListItemResponse;
import com.homeprotectors.backend.dto.stock.StockCreateRequest;
import com.homeprotectors.backend.dto.stock.StockListItemResponse;
import com.homeprotectors.backend.entity.Chore;
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
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Data
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final GroupRepository groupRepository;

    public Stock createStock(@Valid StockCreateRequest request) {
        Stock stock = new Stock();
        stock.setGroupId(1L); // TODO: 임시 group ID
        stock.setCreatedBy(1L); // TODO: 임시 생성자 ID, 실제로는 인증된 사용자 ID로 설정해야 함
        stock.setName(request.getName());
        stock.setUnitQuantity(request.getUnitQuantity());
        stock.setUnit(request.getUnit());
        stock.setUnitDays(request.getUnitDays());
        stock.setReminderDays(request.getReminderDays());
        stock.setUpdatedQuantity(request.getUpdatedQuantity());

        // UpdatedQuantity가 업데이트됐을 때만 현재 시간으로 UpdatedQuantityDate 설정
        if (request.getUpdatedQuantity() != null) {
            stock.setUpdatedQuantityDate(LocalDate.now());
        }

        // nextDue 계산 로직
        // unitQuantity 개에 unitDays 일 & updatedQuantity 따로 입력해서 계산
        // 예: 3개에 7일 & 현재수량: 5개 -> 오늘 + (1개당 2.3일 * 5개) -> 12일 (반올림) -> 7/27
        if (request.getUnitDays() != null && request.getUnitQuantity() != null && request.getUpdatedQuantity() != null) {
            double daysPerUnit = (double) request.getUnitDays() / request.getUnitQuantity();
            double totalDays = daysPerUnit * request.getUpdatedQuantity();
            stock.setNextDue(LocalDate.now().plusDays((int) Math.round(totalDays)));
        } else {
            stock.setNextDue(LocalDate.now()); // 기본값으로 오늘 날짜 설정
        }

        // 현재 시간으로 createdAt 설정
        stock.setCreatedAt(LocalDateTime.now());

        // reminderDate 계산
        if (request.getReminderDays() != null) {
            stock.setReminderDate(stock.getNextDue().minusDays(request.getReminderDays()));
        } else {
            stock.setReminderDate(null); // 미리 알림이 설정되지 않은 경우
        }

        return stockRepository.save(stock);
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
                    LocalDate updatedQuantityDate = stock.getUpdatedQuantityDate();
                    if (updatedQuantityDate == null) {
                        return new StockListItemResponse(
                                stock.getId(),
                                stock.getName(),
                                stock.getUnitQuantity(),
                                stock.getUnit(),
                                stock.getUnitDays(),
                                stock.getUpdatedQuantity(), // 초기값으로 updatedQuantity 사용
                                stock.getNextDue(),
                                stock.getReminderDays()
                        );
                    }

                    long daysSinceUpdate = today.toEpochDay() - updatedQuantityDate.toEpochDay();
                    double dailyConsumption = (double) stock.getUnitQuantity() / stock.getUnitDays();
                    int currentQuantity = Math.max(0, (int) Math.ceil(stock.getUpdatedQuantity() - (daysSinceUpdate * dailyConsumption)));

                    return new StockListItemResponse(
                            stock.getId(),
                            stock.getName(),
                            stock.getUnitQuantity(),
                            stock.getUnit(),
                            stock.getUnitDays(),
                            currentQuantity,
                            stock.getNextDue(),
                            stock.getReminderDays()
                    );
                })
                .collect(Collectors.toList());
    }

    public Stock editStock(Long stockId, StockCreateRequest request) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));

        // 값이 넘어온 것들만 업데이트- 없으면 기존 값 유지
        if (request.getName() != null) {
            stock.setName(request.getName());
        }
        if (request.getUnitQuantity() != null) {
            stock.setUnitQuantity(request.getUnitQuantity());
        }
        if (request.getUnit() != null) {
            stock.setUnit(request.getUnit());
        }
        if (request.getUnitDays() != null) {
            stock.setUnitDays(request.getUnitDays());
        }
        if (request.getUpdatedQuantity() != null) {
            stock.setUpdatedQuantity(request.getUpdatedQuantity());

            // UpdatedQuantity가 업데이트됐을 때만 현재 시간으로 UpdatedQuantityDate 설정
            stock.setUpdatedQuantityDate(LocalDate.now());
        }


        // nextDue 재계산 로직 - 재고의 수량에 따라 다음 소진 예정일을 업데이트
        // unitQuantity 개에 unitDays 일 & currentQuantity 따로 입력해서 계산
        // 3개 중 새로운 입력값이 없는 값은 해당 값은 기존 입력값을 사용해서 계산
        Integer unitDays = (request.getUnitDays() != null) ? request.getUnitDays() : stock.getUnitDays();
        Integer unitQuantity = (request.getUnitQuantity() != null) ? request.getUnitQuantity() : stock.getUnitQuantity();
        Integer updatedQuantity = (request.getUpdatedQuantity() != null) ? request.getUpdatedQuantity() : stock.getUpdatedQuantity();

        if (unitDays != null && unitQuantity != null && updatedQuantity != null) {
            double daysPerUnit = (double) unitDays / unitQuantity;
            double totalDays = daysPerUnit * updatedQuantity;
            stock.setNextDue(LocalDate.now().plusDays((int) Math.round(totalDays)));
        } else {
            stock.setNextDue(LocalDate.now()); // 기본값으로 오늘 날짜 설정
        }

        // reminderDate 계산
        if (request.getReminderDays() != null) {
            stock.setReminderDate(stock.getNextDue().minusDays(request.getReminderDays()));
        } else {
            stock.setReminderDate(null); // 미리 알림이 설정되지 않은 경우
        }

        return stockRepository.save(stock);
    }

    public void deleteStock(Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found"));

        // TODO: 인증 사용자 그룹 소속 여부 확인 (JWT 인증 기반으로 구현 예정)
        stockRepository.delete(stock);
    }
}
