package com.homeprotectors.backend.service;

import com.homeprotectors.backend.entity.Chore;
import com.homeprotectors.backend.entity.Stock;
import com.homeprotectors.backend.repository.ChoreRepository;
import com.homeprotectors.backend.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class DefaultItemSeedService {

    private final DefaultItemTemplateService defaultItemTemplateService;
    private final ChoreRepository choreRepository;
    private final StockRepository stockRepository;
    private final ChoreScheduleCalculator choreScheduleCalculator;

    public DefaultItemSeedService(
            DefaultItemTemplateService defaultItemTemplateService,
            ChoreRepository choreRepository,
            StockRepository stockRepository,
            ChoreScheduleCalculator choreScheduleCalculator
    ) {
        this.defaultItemTemplateService = defaultItemTemplateService;
        this.choreRepository = choreRepository;
        this.stockRepository = stockRepository;
        this.choreScheduleCalculator = choreScheduleCalculator;
    }

    public void seedDefaults(Long groupId, Long userId) {
        var template = defaultItemTemplateService.getTemplate();
        var now = LocalDateTime.now();
        var today = LocalDate.now();

        var chores = template.chores() == null ? Collections.<Chore>emptyList() : template.chores().stream()
                .map(item -> {
                    Chore chore = new Chore();
                    chore.setGroupId(groupId);
                    chore.setTitle(item.title());
                    chore.setRecurrenceType(item.recurrenceType());
                    chore.setSelectedCycle(item.selectedCycle() == null ? Collections.emptySet() : item.selectedCycle());
                    chore.setRoomCategory(item.roomCategory());
                    chore.setCreatedBy(userId);
                    chore.setCreatedAt(now);
                    chore.setNextDue(choreScheduleCalculator.calculateInitialNextDue(chore.getRecurrenceType(), chore.getSelectedCycle()));
                    return chore;
                })
                .toList();

        var stocks = template.stocks() == null ? Collections.<Stock>emptyList() : template.stocks().stream()
                .map(item -> {
                    Stock stock = new Stock();
                    stock.setGroupId(groupId);
                    stock.setName(item.name());
                    stock.setUnitQuantity(item.unitQuantity());
                    stock.setUnitDays(item.unitDays());
                    stock.setCreatedBy(userId);
                    stock.setCreatedAt(now);
                    stock.setUpdatedQuantity(item.updatedQuantity());
                    stock.setUpdatedQuantityDate(today);
                    return stock;
                })
                .toList();

        if (!chores.isEmpty()) {
            choreRepository.saveAll(chores);
        }
        if (!stocks.isEmpty()) {
            stockRepository.saveAll(stocks);
        }
    }
}
