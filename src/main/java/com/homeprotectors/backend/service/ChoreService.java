package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.chore.*;
import com.homeprotectors.backend.entity.Chore;
import com.homeprotectors.backend.entity.RecurrenceType;
import com.homeprotectors.backend.repository.ChoreRepository;
import com.homeprotectors.backend.repository.GroupRepository;
import com.homeprotectors.backend.repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChoreService {

    private final ChoreRepository choreRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;
    private final GroupRepository groupRepository; // TODO: 그룹 기능 추후 추가
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private void validateCycle(ChoreCreateRequest req) {
        if (!req.getRecurrenceType().isValidSelection(req.getSelectedCycle())) {
            throw new IllegalArgumentException("Invalid selectedCycle for recurrenceType "
                    + req.getRecurrenceType());
        }
    }

    /**
     * Chore 생성
     */
    public Chore createChore(@Valid ChoreCreateRequest request) {
        // selectedCycle 유효성 검사
        validateCycle(request);

        Chore chore = new Chore();
        chore.setGroupId(1L); // TODO: JWT 인증 적용 후 동적 값
        chore.setTitle(request.getTitle());
        chore.setRecurrenceType(request.getRecurrenceType());
        chore.setSelectedCycle(request.getSelectedCycle());
        chore.setRoomCategory(request.getRoomCategory());
        chore.setCreatedBy(1L); // TODO: JWT 인증 적용 후 동적 값
        chore.setCreatedAt(LocalDateTime.now());

        // nextDue 초기 계산
        chore.setNextDue(calculateInitialNextDue(request.getRecurrenceType(), request.getSelectedCycle()));

        return choreRepository.save(chore);
    }

    /**
     * Chore 목록 조회
     */
    public List<ChoreListItemResponse> getChoreList() {
        Long groupId = 1L; // TODO: JWT 인증 기반으로 가져오기
        List<Chore> chores = choreRepository.findByGroupId(groupId);

        return chores.stream()
                .map(c -> new ChoreListItemResponse(
                        c.getId(),
                        c.getTitle(),
                        c.getRecurrenceType(),
                        c.getSelectedCycle(),
                        c.getRoomCategory(),
                        c.getNextDue(),
                        false,
                        null
                ))
                .collect(Collectors.toList());
    }

    /**
     * Chore 수정
     */
    public Chore editChore(Long choreId, ChoreEditRequest request) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new EntityNotFoundException("Chore not found"));

        if (request.getTitle() != null) chore.setTitle(request.getTitle());
        if (request.getRoomCategory() != null) chore.setRoomCategory(request.getRoomCategory());

        boolean ruleChanged = (request.getRecurrenceType() != null || request.getSelectedCycle() != null);
        if (ruleChanged) {
            // 검증용 임시 DTO 구성
            ChoreCreateRequest tmp = new ChoreCreateRequest();
            tmp.setRecurrenceType(
                    request.getRecurrenceType() != null ? request.getRecurrenceType() : chore.getRecurrenceType()
            );
            tmp.setSelectedCycle(
                    request.getSelectedCycle() != null ? request.getSelectedCycle() : chore.getSelectedCycle()
            );
            validateCycle(tmp); // selectedCycle 유효성 검사

            if (request.getRecurrenceType() != null) chore.setRecurrenceType(request.getRecurrenceType());
            if (request.getSelectedCycle() != null) chore.setSelectedCycle(request.getSelectedCycle());

            chore.setNextDue(calculateInitialNextDue(chore.getRecurrenceType(), chore.getSelectedCycle()));
        }

        return choreRepository.save(chore);
    }

    /**
     * Chore 삭제
     */
    public void deleteChore(Long choreId) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new EntityNotFoundException("Chore not found"));
        choreRepository.delete(chore);
    }

    /**
     * Chore 완료 → 다음 주기 계산
     */
    public ChoreCompleteResponse completeChore(@Valid ChoreCompleteRequest request) {
        Chore chore = choreRepository.findById(request.getChoreId())
                .orElseThrow(() -> new EntityNotFoundException("Chore not found"));

        LocalDate doneDate = request.getDoneDate();
        LocalDate today = LocalDate.now(KST);

        if (doneDate.isAfter(today)) {
            throw new IllegalArgumentException("완료 날짜는 미래일 수 없습니다.");
        }

        // 완료 후 다음 nextDue 계산
        LocalDate newNextDue = calculateNextDue(chore.getRecurrenceType(), chore.getSelectedCycle(), doneDate);
        chore.setNextDue(newNextDue);

        choreRepository.save(chore);

        return new ChoreCompleteResponse(
                chore.getId(),
                chore.getGroupId(),
                chore.getTitle(),
                chore.getRoomCategory(),
                chore.getRecurrenceType(),
                newNextDue
        );
    }


    /**
     * Main View용 섹션별 Chore 목록 조회
     */
    public ChoreSectionsResponse getChoreSections(int limitPerSection) {
        var now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        var weekStart = now.with(DayOfWeek.MONDAY);
        var weekEnd   = weekStart.plusDays(6);
        var monthStart = now.withDayOfMonth(1);
        var monthEnd   = monthStart.plusMonths(1).minusDays(1);
        var nextMonthEnd = monthStart.plusMonths(2).minusDays(1);

        var rows = choreRepository.findByGroupIdAndNextDueBetweenOrNextDueBefore(
                1L, weekStart, nextMonthEnd, now
        );

        var overdue   = new ArrayList<ChoreListItemResponse>();
        var thisWeek  = new ArrayList<ChoreListItemResponse>();
        var nextWeek  = new ArrayList<ChoreListItemResponse>();
        var thisMonth = new ArrayList<ChoreListItemResponse>();
        var nextMonth = new ArrayList<ChoreListItemResponse>();

        var nextWeekStart = weekStart.plusWeeks(1);
        var nextWeekEnd   = nextWeekStart.plusDays(6);

        for (var c : rows) {
            var d = c.getNextDue();
            var item = new ChoreListItemResponse(c.getId(), c.getTitle(), c.getRecurrenceType(), c.getSelectedCycle(), c.getRoomCategory(), d, false, null);
            if (d.isBefore(now)) overdue.add(item);
            else if (!d.isBefore(weekStart) && !d.isAfter(weekEnd)) thisWeek.add(item);
            else if (!d.isBefore(nextWeekStart) && !d.isAfter(nextWeekEnd)) nextWeek.add(item);
            else if (!d.isBefore(monthStart) && !d.isAfter(monthEnd)) thisMonth.add(item);
            else nextMonth.add(item);
        }

        Comparator<ChoreListItemResponse> cmp = Comparator
                .comparing(ChoreListItemResponse::getNextDue)
                .thenComparing(ChoreListItemResponse::getId);
        overdue.sort(Comparator.comparing(ChoreListItemResponse::getNextDue));
        thisWeek.sort(cmp); nextWeek.sort(cmp); thisMonth.sort(cmp); nextMonth.sort(cmp);

        var thisWeekMerged = new ArrayList<ChoreListItemResponse>(overdue);
        thisWeekMerged.addAll(thisWeek);

        // --- shopping: collect stocks with remainingDays <= 7 ---
        var allStocks = stockRepository.findByGroupId(1L);

        var shoppingItems = allStocks.stream()
                .map(s -> {
                    LocalDate today = LocalDate.now();
                    LocalDate updatedDate = s.getUpdatedQuantityDate();
                    long daysSinceUpdate = today.toEpochDay() - updatedDate.toEpochDay();
                    double dailyConsumption = (double) s.getUnitQuantity() / s.getUnitDays();

                    double currentDouble = s.getUpdatedQuantity() - (daysSinceUpdate * dailyConsumption);
                    int currentQuantity = Math.max(0, (int) Math.ceil(currentDouble));

                    int remainingDays = (dailyConsumption > 0)
                            ? Math.max(0, (int) Math.round(currentQuantity / dailyConsumption))
                            : 0; // 0 division 방지

                    return new ChoreShoppingItemResponse(
                            s.getId(),
                            s.getName(),
                            currentQuantity,
                            remainingDays
                    );
                })
                // 여기서 remainingDays 기준으로 필터
                .filter(item -> item.getRemainingDays() <= 7)
                .sorted(Comparator.comparing(ChoreShoppingItemResponse::getRemainingDays)
                        .thenComparing(ChoreShoppingItemResponse::getId))
                .collect(Collectors.toList());

        if (!shoppingItems.isEmpty()) {
            // 장보기 컨테이너 아이템을 생성해서 thisWeek 섹션에 추가
            var shoppingContainer = new ChoreListItemResponse();
            shoppingContainer.setId(0L);
            shoppingContainer.setTitle("장보기");
            shoppingContainer.setShoppingContainer(true);
            shoppingContainer.setShoppingItems(shoppingItems);

            thisWeekMerged.add(shoppingContainer);
        }

        return ChoreSectionsResponse.builder()
                .sections(ChoreSectionsResponse.Sections.builder()
                        .thisWeek(section(thisWeekMerged, limitPerSection))
                        .nextWeek(section(nextWeek, limitPerSection))
                        .thisMonth(section(thisMonth, limitPerSection))
                        .nextMonth(section(nextMonth, limitPerSection))
                        .build())
                .build();
    }

    private ChoreSectionsResponse.Section section(List<ChoreListItemResponse> items, int limit) {
        var view = items.size() > limit ? items.subList(0, limit) : items;
        return ChoreSectionsResponse.Section.builder()
                .count(view.size())
                .items(view)
                .build();
    }

    /**
     * 생성 시 최초 nextDue 계산
     */
    private LocalDate calculateInitialNextDue(RecurrenceType type, Set<String> selectedCycle) {
        LocalDate today = LocalDate.now(KST);
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd   = weekStart.plusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd   = monthStart.plusMonths(1).minusDays(1);

        return switch (type) {
            // 간격형: 생성 시 1회 클램프 유지
            case PER_WEEK -> min(today.plusDays(7), weekEnd);
            case PER_2WEEKS -> min(today.plusDays(14), monthEnd);
            case PER_MONTH -> min(today.plusDays(30), monthEnd);

            // 고정형: “다음 자연 발생일”
            case FIXED_DAY -> findClosestDayOfWeek(today, selectedCycle);
            case FIXED_DATE -> nextDateByDateTokens(today, selectedCycle);
            case FIXED_MONTH -> nextDateBySelectedMonths(today, selectedCycle /*defaultDay*/);
        };
    }

    private static LocalDate min(LocalDate a, LocalDate b) { return a.isAfter(b) ? b : a; }


    /**
     * 완료 후 nextDue 계산
     */
    private LocalDate calculateNextDue(RecurrenceType type, Set<String> selectedCycle, LocalDate doneDate) {
        switch (type) {
            case PER_WEEK:
                return doneDate.plusDays(7);
            case PER_2WEEKS:
                return doneDate.plusDays(14);
            case PER_MONTH:
                return doneDate.plusDays(30);
            case FIXED_DAY:
                return findNextDayOfWeek(doneDate, selectedCycle);
            case FIXED_DATE:
                int day = doneDate.getDayOfMonth();
                LocalDate nextMonth = doneDate.plusMonths(1);
                return nextMonth.withDayOfMonth(Math.min(day, nextMonth.lengthOfMonth()));
            case FIXED_MONTH:
                int anchor = doneDate.getDayOfMonth();
                LocalDate candidate = findNextSelectedMonth(doneDate, selectedCycle);
                return candidate.withDayOfMonth(Math.min(anchor, candidate.lengthOfMonth()));
            default:
                return doneDate;
        }
    }

    private LocalDate findClosestDayOfWeek(LocalDate today, Set<String> selectedCycle) {
        for (int i = 0; i < 7; i++) {
            LocalDate candidate = today.plusDays(i);
            if (selectedCycle.contains(candidate.getDayOfWeek().name())) {
                return candidate;
            }
        }
        return today;
    }

    private LocalDate findNextDayOfWeek(LocalDate doneDate, Set<String> selectedCycle) {
        for (int i = 1; i <= 7; i++) {
            LocalDate candidate = doneDate.plusDays(i);
            if (selectedCycle.contains(candidate.getDayOfWeek().name())) {
                return candidate;
            }
        }
        return doneDate.plusDays(7);
    }

    private LocalDate findNextSelectedMonth(LocalDate doneDate, Set<String> selectedCycle) {
        int currentYear = doneDate.getYear();
        int currentMonth = doneDate.getMonthValue();

        for (int i = 1; i <= 12; i++) {
            LocalDate candidate = doneDate.plusMonths(i);
            if (selectedCycle.contains(String.valueOf(candidate.getMonthValue()))) {
                return candidate;
            }
        }
        return LocalDate.of(currentYear + 1, Integer.parseInt(selectedCycle.iterator().next()), 1);
    }

    // FIXED_DATE: selectedCycle에 "END" 또는 "1".."30"이 들어온다는 전제
    private LocalDate nextDateByDateTokens(LocalDate base, Set<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return base; // 검증이 막아주지만 가드
        // 후보일(이번 달 기준) 계산
        List<LocalDate> cands = new ArrayList<>();
        int lenThis = base.lengthOfMonth();

        for (String t : tokens) {
            if ("END".equalsIgnoreCase(t)) {
                cands.add(base.withDayOfMonth(lenThis)); // 이번 달 말일
            } else {
                try {
                    int dom = Integer.parseInt(t);
                    if (dom >= 1 && dom <= 30) {
                        int day = Math.min(dom, lenThis);
                        cands.add(base.withDayOfMonth(day));
                    }
                } catch (NumberFormatException ignore) {}
            }
        }
        // 오늘 이전 후보는 다음 달로 이월
        LocalDate best = null;
        for (LocalDate d : cands) {
            LocalDate dd = !d.isBefore(base) ? d : safelyDom(base.plusMonths(1), d.getDayOfMonth());
            if (best == null || dd.isBefore(best)) best = dd;
        }
        return best != null ? best : base;
    }

    // FIXED_MONTH: 선택 월들 중 base 이후 가장 가까운 월의 날짜를 반환  (기본은 1일)
    private LocalDate nextDateBySelectedMonths(LocalDate base, Set<String> months) {
        if (months == null || months.isEmpty()) return base; // 검증 가드
        for (int i = 0; i <= 24; i++) {
            LocalDate cand = base.plusMonths(i);
            String m = String.valueOf(cand.getMonthValue()); // "1".."12"
            if (months.contains(m)) {
                int len = cand.lengthOfMonth();
                LocalDate d = cand.withDayOfMonth(Math.min(1, len));
                if (!d.isBefore(base)) return d;
            }
        }
        // 이론상 도달 X
        LocalDate n = base.plusMonths(1);
        return n.withDayOfMonth(1);
    }

    // 월 길이에 맞춰 일 보정
    private LocalDate safelyDom(LocalDate any, int dom) {
        int len = any.lengthOfMonth();
        return any.withDayOfMonth(Math.min(dom, len));
    }

}