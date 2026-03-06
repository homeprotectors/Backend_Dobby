package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.chore.ChoreCompleteRequest;
import com.homeprotectors.backend.dto.chore.ChoreCompleteResponse;
import com.homeprotectors.backend.dto.chore.ChoreCreateRequest;
import com.homeprotectors.backend.dto.chore.ChoreEditRequest;
import com.homeprotectors.backend.dto.chore.ChoreListItemResponse;
import com.homeprotectors.backend.dto.chore.ChoreSectionsResponse;
import com.homeprotectors.backend.dto.chore.ChoreShoppingItemResponse;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChoreService {

    private final ChoreRepository choreRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;
    private final GroupRepository groupRepository;
    private final UserContextService userContextService;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private void validateCycle(ChoreCreateRequest req) {
        if (!req.getRecurrenceType().isValidSelection(req.getSelectedCycle())) {
            throw new IllegalArgumentException("Invalid selectedCycle for recurrenceType " + req.getRecurrenceType());
        }
    }

    public Chore createChore(@Valid ChoreCreateRequest request, UUID currentUserId) {
        validateCycle(request);
        Long groupId = userContextService.requireGroupId(currentUserId);
        Long userId = userContextService.requireInternalUserId(currentUserId);

        Chore chore = new Chore();
        chore.setGroupId(groupId);
        chore.setTitle(request.getTitle());
        chore.setRecurrenceType(request.getRecurrenceType());
        chore.setSelectedCycle(request.getSelectedCycle());
        chore.setRoomCategory(request.getRoomCategory());
        chore.setCreatedBy(userId);
        chore.setCreatedAt(LocalDateTime.now());
        chore.setNextDue(calculateInitialNextDue(request.getRecurrenceType(), request.getSelectedCycle()));
        return choreRepository.save(chore);
    }

    public List<ChoreListItemResponse> getChoreList(UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
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

    public Chore editChore(Long choreId, ChoreEditRequest request, UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
        Chore chore = choreRepository.findByIdAndGroupId(choreId, groupId)
                .orElseThrow(() -> new EntityNotFoundException("Chore not found"));

        if (request.getTitle() != null) chore.setTitle(request.getTitle());
        if (request.getRoomCategory() != null) chore.setRoomCategory(request.getRoomCategory());

        boolean ruleChanged = (request.getRecurrenceType() != null || request.getSelectedCycle() != null);
        if (ruleChanged) {
            ChoreCreateRequest tmp = new ChoreCreateRequest();
            tmp.setRecurrenceType(request.getRecurrenceType() != null ? request.getRecurrenceType() : chore.getRecurrenceType());
            tmp.setSelectedCycle(request.getSelectedCycle() != null ? request.getSelectedCycle() : chore.getSelectedCycle());
            validateCycle(tmp);

            if (request.getRecurrenceType() != null) chore.setRecurrenceType(request.getRecurrenceType());
            if (request.getSelectedCycle() != null) chore.setSelectedCycle(request.getSelectedCycle());
            chore.setNextDue(calculateInitialNextDue(chore.getRecurrenceType(), chore.getSelectedCycle()));
        }

        return choreRepository.save(chore);
    }

    public void deleteChore(Long choreId, UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
        Chore chore = choreRepository.findByIdAndGroupId(choreId, groupId)
                .orElseThrow(() -> new EntityNotFoundException("Chore not found"));
        choreRepository.delete(chore);
    }

    public ChoreCompleteResponse completeChore(@Valid ChoreCompleteRequest request, UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
        Chore chore = choreRepository.findByIdAndGroupId(request.getChoreId(), groupId)
                .orElseThrow(() -> new EntityNotFoundException("Chore not found"));
        var groupPublicId = userContextService.requireGroupPublicId(currentUserId);

        LocalDate doneDate = request.getDoneDate();
        LocalDate today = LocalDate.now(KST);
        if (doneDate.isAfter(today)) {
            throw new IllegalArgumentException("doneDate cannot be in the future.");
        }

        LocalDate newNextDue = calculateNextDue(chore.getRecurrenceType(), chore.getSelectedCycle(), doneDate);
        chore.setNextDue(newNextDue);
        choreRepository.save(chore);

        return new ChoreCompleteResponse(
                chore.getId(),
                groupPublicId,
                chore.getTitle(),
                chore.getRoomCategory(),
                chore.getRecurrenceType(),
                newNextDue
        );
    }

    public ChoreSectionsResponse getChoreSections(int limitPerSection, UUID currentUserId) {
        Long groupId = userContextService.requireGroupId(currentUserId);
        var now = LocalDate.now(KST);
        var weekStart = now.with(DayOfWeek.MONDAY);
        var weekEnd = weekStart.plusDays(6);
        var monthStart = now.withDayOfMonth(1);
        var monthEnd = monthStart.plusMonths(1).minusDays(1);
        var nextMonthEnd = monthStart.plusMonths(2).minusDays(1);

        var rows = choreRepository.findByGroupIdAndNextDueBetweenOrNextDueBefore(groupId, weekStart, nextMonthEnd, now);

        var overdue = new ArrayList<ChoreListItemResponse>();
        var thisWeek = new ArrayList<ChoreListItemResponse>();
        var nextWeek = new ArrayList<ChoreListItemResponse>();
        var thisMonth = new ArrayList<ChoreListItemResponse>();
        var nextMonth = new ArrayList<ChoreListItemResponse>();

        var nextWeekStart = weekStart.plusWeeks(1);
        var nextWeekEnd = nextWeekStart.plusDays(6);

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
        thisWeek.sort(cmp);
        nextWeek.sort(cmp);
        thisMonth.sort(cmp);
        nextMonth.sort(cmp);

        var thisWeekMerged = new ArrayList<>(overdue);
        thisWeekMerged.addAll(thisWeek);

        var allStocks = stockRepository.findByGroupId(groupId);
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
                            : 0;

                    return new ChoreShoppingItemResponse(
                            s.getId(),
                            s.getName(),
                            currentQuantity,
                            remainingDays
                    );
                })
                .filter(item -> item.getRemainingDays() <= 7)
                .sorted(Comparator.comparing(ChoreShoppingItemResponse::getRemainingDays).thenComparing(ChoreShoppingItemResponse::getId))
                .collect(Collectors.toList());

        if (!shoppingItems.isEmpty()) {
            var shoppingContainer = new ChoreListItemResponse();
            shoppingContainer.setId(0L);
            shoppingContainer.setTitle("Shopping");
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

    private LocalDate calculateInitialNextDue(RecurrenceType type, Set<String> selectedCycle) {
        LocalDate today = LocalDate.now(KST);
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        return switch (type) {
            case PER_WEEK -> min(today.plusDays(7), weekEnd);
            case PER_2WEEKS -> min(today.plusDays(14), monthEnd);
            case PER_MONTH -> min(today.plusDays(30), monthEnd);
            case FIXED_DAY -> findClosestDayOfWeek(today, selectedCycle);
            case FIXED_DATE -> nextDateByDateTokens(today, selectedCycle);
            case FIXED_MONTH -> nextDateBySelectedMonths(today, selectedCycle);
        };
    }

    private static LocalDate min(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? b : a;
    }

    private LocalDate calculateNextDue(RecurrenceType type, Set<String> selectedCycle, LocalDate doneDate) {
        return switch (type) {
            case PER_WEEK -> doneDate.plusDays(7);
            case PER_2WEEKS -> doneDate.plusDays(14);
            case PER_MONTH -> doneDate.plusDays(30);
            case FIXED_DAY -> findNextDayOfWeek(doneDate, selectedCycle);
            case FIXED_DATE -> {
                int day = doneDate.getDayOfMonth();
                LocalDate nextMonth = doneDate.plusMonths(1);
                yield nextMonth.withDayOfMonth(Math.min(day, nextMonth.lengthOfMonth()));
            }
            case FIXED_MONTH -> {
                int anchor = doneDate.getDayOfMonth();
                LocalDate candidate = findNextSelectedMonth(doneDate, selectedCycle);
                yield candidate.withDayOfMonth(Math.min(anchor, candidate.lengthOfMonth()));
            }
        };
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
        for (int i = 1; i <= 12; i++) {
            LocalDate candidate = doneDate.plusMonths(i);
            if (selectedCycle.contains(String.valueOf(candidate.getMonthValue()))) {
                return candidate;
            }
        }
        return LocalDate.of(currentYear + 1, Integer.parseInt(selectedCycle.iterator().next()), 1);
    }

    private LocalDate nextDateByDateTokens(LocalDate base, Set<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return base;
        List<LocalDate> cands = new ArrayList<>();
        int lenThis = base.lengthOfMonth();

        for (String t : tokens) {
            if ("END".equalsIgnoreCase(t)) {
                cands.add(base.withDayOfMonth(lenThis));
            } else {
                try {
                    int dom = Integer.parseInt(t);
                    if (dom >= 1 && dom <= 30) {
                        int day = Math.min(dom, lenThis);
                        cands.add(base.withDayOfMonth(day));
                    }
                } catch (NumberFormatException ignore) {
                }
            }
        }

        LocalDate best = null;
        for (LocalDate d : cands) {
            LocalDate dd = !d.isBefore(base) ? d : safelyDom(base.plusMonths(1), d.getDayOfMonth());
            if (best == null || dd.isBefore(best)) best = dd;
        }
        return best != null ? best : base;
    }

    private LocalDate nextDateBySelectedMonths(LocalDate base, Set<String> months) {
        if (months == null || months.isEmpty()) return base;
        for (int i = 0; i <= 24; i++) {
            LocalDate cand = base.plusMonths(i);
            String m = String.valueOf(cand.getMonthValue());
            if (months.contains(m)) {
                int len = cand.lengthOfMonth();
                LocalDate d = cand.withDayOfMonth(Math.min(1, len));
                if (!d.isBefore(base)) return d;
            }
        }
        LocalDate n = base.plusMonths(1);
        return n.withDayOfMonth(1);
    }

    private LocalDate safelyDom(LocalDate any, int dom) {
        int len = any.lengthOfMonth();
        return any.withDayOfMonth(Math.min(dom, len));
    }
}
