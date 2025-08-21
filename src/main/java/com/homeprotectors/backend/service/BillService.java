package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.bill.BillCreateRequest;
import com.homeprotectors.backend.entity.Bill;
import com.homeprotectors.backend.repository.BillHistoryRepository;
import com.homeprotectors.backend.repository.BillRepository;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Data
@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final BillHistoryRepository billHistoryRepository;

    public Bill createBill(@Valid BillCreateRequest request) {
        Bill bill = new Bill();
        // Set the group ID and createdBy to temporary values for now
        bill.setGroupId(1L); // TODO: 임시 group ID
        bill.setName(request.getName());
        bill.setAmount(request.getAmount());

        if (request.getDueDate() >= 1 && request.getDueDate() <= 31) { // 청구일은 1-31 사이의 값만
            bill.setDueDate(request.getDueDate());
        } else {
            throw new IllegalArgumentException("청구 일자는 1에서 31 사이의 값이어야 합니다.");
        }

        bill.setCreatedBy(1L); // TODO: 임시 생성자 ID, 실제로는 인증된 사용자 ID로 설정해야 함
        bill.setIsVariable(request.getIsVariable());

        if (request.getReminderDays() != null) { // 미리 알림 일수 설정
            bill.setReminderDays(request.getReminderDays());

            // 미리 알림 날짜 계산 (매월 청구 일자 - 미리 알림 일수) 예: Due Date가 30일이고 Reminder Days가 2일이면 Reminder Date는 28일
            LocalDate dueDate;
            if (LocalDate.now().lengthOfMonth() > request.getDueDate()) {
                // 현재 달의 dueDate가 있는 경우, 해당 월의 dueDate로 설정
                dueDate = LocalDate.now().withDayOfMonth(request.getDueDate());
            } else {
                // 현재 달의 dueDate가 없는 경우, 해당 월의 마지막 날로 설정
                dueDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            }

            bill.setReminderDate(dueDate.minusDays(request.getReminderDays()));

        } else {
            bill.setReminderDays(null);
            bill.setReminderDate(null);
        }

        return billRepository.save(bill);

    }
}
