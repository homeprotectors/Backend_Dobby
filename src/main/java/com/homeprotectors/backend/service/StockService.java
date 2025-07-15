package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.chore.ChoreListItemResponse;
import com.homeprotectors.backend.dto.stock.StockCreateRequest;
import com.homeprotectors.backend.dto.stock.StockListItemResponse;
import com.homeprotectors.backend.entity.Chore;
import com.homeprotectors.backend.entity.Stock;
import com.homeprotectors.backend.repository.GroupRepository;
import com.homeprotectors.backend.repository.StockRepository;
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

    public Stock createStock(@Valid StockCreateRequest request) {
        Stock stock = new Stock();
        stock.setGroupId(1L); // TODO: 임시 group ID
        stock.setCreatedBy(1L); // TODO: 임시 생성자 ID, 실제로는 인증된 사용자 ID로 설정해야 함
        stock.setName(request.getName());
        stock.setUnitQuantity(request.getUnitQuantity());
        stock.setUnit(request.getUnit());
        stock.setUnitDays(request.getUnitDays());
        stock.setReminderDays(request.getReminderDays());

        // nextDue 계산 로직
        stock.setNextDue(LocalDate.now().plusDays(request.getUnitDays()));

        // 현재 시간으로 lastUpdated 설정
        stock.setLastUpdated(LocalDateTime.now());

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
        return stocks.stream()
                .map(stock -> new StockListItemResponse(
                        stock.getId(),
                        stock.getName(),
                        stock.getUnitQuantity(),
                        stock.getUnit(),
                        stock.getUnitDays(),
                        stock.getNextDue(),
                        stock.getReminderDays()
                ))
                .collect(Collectors.toList());
    }
}
