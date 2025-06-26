package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.stock.StockCreateRequest;
import com.homeprotectors.backend.entity.Stock;
import com.homeprotectors.backend.repository.GroupRepository;
import com.homeprotectors.backend.repository.StockRepository;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        stock.setQuantity(request.getQuantity());
        stock.setUnit(request.getUnit());
        stock.setEstimatedConsumptionDays(request.getEstimatedConsumptionDays());
        stock.setReminderDays(request.getReminderDays());

        // nextDue 계산 로직
        stock.setNextDue(LocalDate.now().plusDays(request.getEstimatedConsumptionDays()));

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
}
