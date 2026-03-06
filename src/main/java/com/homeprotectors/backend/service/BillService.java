package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.bill.BillCreateRequest;
import com.homeprotectors.backend.dto.bill.BillEditRequest;
import com.homeprotectors.backend.dto.bill.BillListItemResponse;
import com.homeprotectors.backend.dto.bill.BillListViewResponse;
import com.homeprotectors.backend.entity.Bill;
import com.homeprotectors.backend.entity.BillHistory;
import com.homeprotectors.backend.entity.BillType;
import com.homeprotectors.backend.repository.BillHistoryRepository;
import com.homeprotectors.backend.repository.BillRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.UUID;

@Data
@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final BillHistoryRepository billHistoryRepository;
    private final UserContextService userContextService;

    @Transactional
    public Bill createBill(@Valid BillCreateRequest req, UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
        Long userId = userContextService.requireInternalUserId(currentUserId);
        Bill b = new Bill();
        b.setGroupId(groupId);
        b.setCreatedBy(userId);
        b.setName(req.getName());
        b.setAmount(req.getAmount());
        b.setIsVariable(req.getIsVariable());
        if (req.getDueDate() != null) {
            int due = req.getDueDate();
            if (due < 1 || due > 31) throw new IllegalArgumentException("dueDate는 1~31 또는 null");
            b.setDueDate(due);
        } else {
            b.setDueDate(null);
        }
        return billRepository.save(b);
    }

    @Transactional
    public Bill updateBill(Long billId, @Valid BillEditRequest req, UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
        Bill b = billRepository.findByIdAndGroupId(billId, groupId)
                .orElseThrow(() -> new NoSuchElementException("bill not found"));

        if (req.getName() != null) b.setName(req.getName());
        if (req.getAmount() != null) b.setAmount(req.getAmount());
        if (req.getIsVariable() != null) b.setIsVariable(req.getIsVariable());
        if (req.getDueDate() != null) {
            int due = req.getDueDate();
            if (due < 1 || due > 31) throw new IllegalArgumentException("dueDate는 1~31 또는 null");
            b.setDueDate(due);
        }

        return billRepository.save(b);
    }

    @Transactional
    public void softDeleteBill(Long billId, UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
        Bill b = billRepository.findByIdAndGroupId(billId, groupId)
                .orElseThrow(() -> new NoSuchElementException("bill not found"));
        billRepository.delete(b); // @SQLDelete 로 deleted_at=now()
    }
    @Transactional
    public void hideBill(Long billId, UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
        Bill b = billRepository.findByIdAndGroupId(billId, groupId)
                .orElseThrow(() -> new NoSuchElementException("bill not found"));
        LocalDate hiddenFrom = LocalDate.now().withDayOfMonth(1);
        b.setHiddenFrom(hiddenFrom);
        billRepository.save(b);
    }

    @Transactional
    public void unhideBill(Long billId, UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
        Bill b = billRepository.findByIdAndGroupId(billId, groupId)
                .orElseThrow(() -> new NoSuchElementException("bill not found"));
        b.setHiddenFrom(null);
        billRepository.save(b);
    }

    @Transactional
    public BillListViewResponse getListView(String monthStr, UUID currentUserId) {
        // month 파싱 → 그 달 1일
        LocalDate month = LocalDate.parse(monthStr + "-01");
        LocalDate prev = month.minusMonths(1);

        Long groupId = userContextService.requireGroupId(currentUserId);

        // 전체 bills (soft-deleted 제외)
        List<Bill> bills = billRepository.findByGroupId(groupId);
        int totalCount = bills.size();

        // 해당월/전월 history 맵(billId -> amount)
        Map<Long, Double> histMonth = billHistoryRepository
                .findByGroupIdAndYearMonth(groupId, month)
                .stream()
                .collect(Collectors.toMap(h -> h.getBill().getId(), BillHistory::getAmount, (a, b) -> b));
        Map<Long, Double> histPrev = billHistoryRepository
                .findByGroupIdAndYearMonth(groupId, prev)
                .stream()
                .collect(Collectors.toMap(h -> h.getBill().getId(), BillHistory::getAmount, (a, b) -> b));

        // 섹션 분류 함수: 이제는 isVariable 기준으로만 분류
        Function<Bill, String> sectionOf = b -> b.getIsVariable() ? "VARIABLE" : "FIXED";

        // 정렬: dueDate asc, name asc
        Comparator<Bill> order = Comparator
                .comparing((Bill b) -> Optional.ofNullable(b.getDueDate()).orElse(32))
                .thenComparing(Bill::getName, Comparator.nullsLast(String::compareTo));

        List<Bill> sorted = bills.stream().sorted(order).toList();

        List<BillListItemResponse> fixed = new ArrayList<>();
        List<BillListItemResponse> variable = new ArrayList<>();

        double monthTotal = 0;
        double prevTotal = 0;

        for (Bill b : sorted) {
            // 숨김 처리: hiddenFrom가 설정되어 있고 요청한 연월이 그 연월(또는 이후)이면 항목을 노출하지 않음
            if (b.getHiddenFrom() != null && !month.isBefore(b.getHiddenFrom())) {
                continue;
            }

            // 해당월 아이템 금액 표시 규칙
            double thisMonthAmount = b.getIsVariable()
                    ? histMonth.getOrDefault(b.getId(), 0.0)
                    : Optional.ofNullable(b.getAmount()).orElse(0.0);

            double prevMonthAmount = b.getIsVariable()
                    ? histPrev.getOrDefault(b.getId(), 0.0)
                    : Optional.ofNullable(b.getAmount()).orElse(0.0);
            monthTotal += thisMonthAmount;
            prevTotal += prevMonthAmount;

            BillListItemResponse item = new BillListItemResponse(
                    b.getId(),
                    b.getName(),
                    b.getDueDate(),
                    thisMonthAmount
            );

            switch (sectionOf.apply(b)) {
                case "FIXED" -> fixed.add(item);
                case "VARIABLE" -> variable.add(item);
            }
        }

        double monDiff = monthTotal - prevTotal;

        BillListViewResponse.Sections sections =
                new BillListViewResponse.Sections(fixed, variable);

        return new BillListViewResponse(monthStr, monthTotal, monDiff, sections);
    }

}
