package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.bill.BillCreateRequest;
import com.homeprotectors.backend.dto.bill.BillEditRequest;
import com.homeprotectors.backend.dto.bill.BillListItemResponse;
import com.homeprotectors.backend.dto.bill.BillListViewResponse;
import com.homeprotectors.backend.entity.Bill;
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

@Data
@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final BillHistoryRepository billHistoryRepository;


    private static final Long GROUP_ID = 1L;
    private static final Long USER_ID = 1L;

    @Transactional
    public Bill createBill(@Valid BillCreateRequest req) {
        Bill b = new Bill();
        b.setGroupId(GROUP_ID);
        b.setCreatedBy(USER_ID);
        b.setName(req.getName());
        b.setCategory(req.getCategory());
        b.setType(req.getType());
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
    public Bill updateBill(Long billId, @Valid BillEditRequest req) {
        Bill b = billRepository.findByIdAndGroupId(billId, GROUP_ID)
                .orElseThrow(() -> new NoSuchElementException("bill not found"));

        if (req.getName() != null) b.setName(req.getName());
        if (req.getCategory() != null) b.setCategory(req.getCategory());
        if (req.getType() != null) b.setType(req.getType());
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
    public void softDeleteBill(Long billId) {
        Bill b = billRepository.findByIdAndGroupId(billId, GROUP_ID)
                .orElseThrow(() -> new NoSuchElementException("bill not found"));
        billRepository.delete(b); // @SQLDelete 로 deleted_at=now()
    }

    @Transactional
    public BillListViewResponse getListView(String monthStr) {
        // month 파싱 → 그 달 1일
        LocalDate month = LocalDate.parse(monthStr + "-01");
        LocalDate prev = month.minusMonths(1);

        Long groupId = GROUP_ID;

        // 전체 bills (soft-deleted 제외)
        List<Bill> bills = billRepository.findByGroupId(groupId);
        int totalCount = bills.size();

        // 해당월/전월 history 맵(billId -> amount)
        Map<Long, Integer> histMonth = billHistoryRepository
                .findByGroupIdAndYearMonth(groupId, month)
                .stream()
                .collect(Collectors.toMap(h -> h.getBill().getId(), h -> h.getAmount(), (a, b) -> b));
        Map<Long, Integer> histPrev = billHistoryRepository
                .findByGroupIdAndYearMonth(groupId, prev)
                .stream()
                .collect(Collectors.toMap(h -> h.getBill().getId(), h -> h.getAmount(), (a, b) -> b));

        // 섹션 분류 함수
        Function<Bill, String> sectionOf = b -> {
            if (b.getType() == BillType.수도세 || b.getType() == BillType.전기세
                    || b.getType() == BillType.가스난방 || b.getType() == BillType.관리비) {
                return "UTILITIES";
            }
            return b.getIsVariable() ? "VARIABLE" : "FIXED";
        };

        // 정렬: dueDate asc, name asc
        Comparator<Bill> order = Comparator
                .comparing((Bill b) -> Optional.ofNullable(b.getDueDate()).orElse(32))
                .thenComparing(Bill::getName, Comparator.nullsLast(String::compareTo));

        List<Bill> sorted = bills.stream().sorted(order).toList();

        List<BillListItemResponse> util = new ArrayList<>();
        List<BillListItemResponse> fixed = new ArrayList<>();
        List<BillListItemResponse> variable = new ArrayList<>();

        int monthTotal = 0;
        int prevTotal = 0;

        for (Bill b : sorted) {
            // 해당월 아이템 금액 표시 규칙
            int thisMonthAmount = b.getIsVariable()
                    ? histMonth.getOrDefault(b.getId(), 0)
                    : Optional.ofNullable(b.getAmount()).orElse(0);

            int prevMonthAmount = b.getIsVariable()
                    ? histPrev.getOrDefault(b.getId(), 0)
                    : Optional.ofNullable(b.getAmount()).orElse(0);

            monthTotal += thisMonthAmount;
            prevTotal += prevMonthAmount;

            BillListItemResponse item = new BillListItemResponse(
                    b.getId(),
                    b.getName(),
                    b.getDueDate(),
                    thisMonthAmount
            );

            switch (sectionOf.apply(b)) {
                case "UTILITIES" -> util.add(item);
                case "FIXED" -> fixed.add(item);
                case "VARIABLE" -> variable.add(item);
            }
        }

        int momDiff = monthTotal - prevTotal;

        BillListViewResponse.Sections sections =
                new BillListViewResponse.Sections(util, fixed, variable);

        return new BillListViewResponse(monthStr, totalCount, monthTotal, momDiff, sections);
    }

}
