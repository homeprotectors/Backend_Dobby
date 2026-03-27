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
    private final ChoreScheduleCalculator choreScheduleCalculator;
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
        chore.setNextDue(choreScheduleCalculator.calculateInitialNextDue(request.getRecurrenceType(), request.getSelectedCycle()));
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
            chore.setNextDue(choreScheduleCalculator.calculateInitialNextDue(chore.getRecurrenceType(), chore.getSelectedCycle()));
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

        LocalDate newNextDue = choreScheduleCalculator.calculateNextDue(chore.getRecurrenceType(), chore.getSelectedCycle(), doneDate);
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

}
