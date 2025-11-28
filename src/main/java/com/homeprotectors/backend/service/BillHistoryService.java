package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.bill.BillHistoryCreateRequest;
import com.homeprotectors.backend.dto.bill.BillHistoryEditRequest;
import com.homeprotectors.backend.entity.Bill;
import com.homeprotectors.backend.entity.BillHistory;
import com.homeprotectors.backend.repository.BillHistoryRepository;
import com.homeprotectors.backend.repository.BillRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BillHistoryService {

    private final BillRepository billRepository;
    private final BillHistoryRepository billHistoryRepository;

    // TODO: 실제 인증/그룹 주입
    private static final Long GROUP_ID = 1L;
    private static final Long USER_ID = 1L;

    @Transactional
    public BillHistory create(@Valid BillHistoryCreateRequest req) {
        Long billId = req.getBillId();

        Bill bill = billRepository.findByIdAndGroupId(billId, GROUP_ID)
                .orElseThrow(() -> new NoSuchElementException("bill not found"));

        LocalDate ym = LocalDate.parse(req.getMonth() + "-01");

        // 중복 선체크(가독성/명시적 409 처리용)
        billHistoryRepository.findByGroupIdAndBill_IdAndYearMonth(GROUP_ID, billId, ym)
                .ifPresent(x -> { throw new IllegalStateException("history exists for month"); });

        BillHistory h = new BillHistory();
        h.setGroupId(GROUP_ID);
        h.setPaidBy(USER_ID);
        h.setBill(bill);
        h.setYearMonth(ym);
        h.setAmount(req.getAmount());

        try {
            return billHistoryRepository.save(h);
        } catch (DataIntegrityViolationException e) {
            // 유니크 제약 위반(경합) → 409로 매핑되도록 컨트롤러에서 잡을 것
            throw new IllegalStateException("history exists for month");
        }
    }

    @Transactional
    public BillHistory update(@Valid BillHistoryEditRequest req) {
        BillHistory h = billHistoryRepository.findByGroupIdAndBill_IdAndYearMonth(
                        GROUP_ID,
                        req.getBillId(),
                        LocalDate.parse(req.getMonth() + "-01"))
                .orElseThrow(() -> new NoSuchElementException("history not found"));

        if (req.getAmount() != null) h.setAmount(req.getAmount());
        return h; // 변경감지로 flush
    }

    @Transactional
    public void delete(Long historyId) {
        BillHistory h = billHistoryRepository.findById(historyId)
                .orElseThrow(() -> new NoSuchElementException("history not found"));
        billHistoryRepository.delete(h);
    }
}
