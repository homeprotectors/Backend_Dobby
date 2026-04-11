package com.homeprotectors.backend.service;

import com.homeprotectors.backend.entity.RecurrenceType;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ChoreScheduleCalculator {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public LocalDate calculateInitialNextDue(RecurrenceType type, Set<String> selectedCycle) {
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

    public LocalDate calculateNextDue(RecurrenceType type, Set<String> selectedCycle, LocalDate doneDate, LocalDate currentNextDue) {
        LocalDate effectiveDue = currentNextDue != null && currentNextDue.isAfter(doneDate) ? currentNextDue : doneDate;
        return switch (type) {
            case PER_WEEK -> effectiveDue.plusDays(7);
            case PER_2WEEKS -> effectiveDue.plusDays(14);
            case PER_MONTH -> effectiveDue.plusDays(30);
            case FIXED_DAY -> findNextDayOfWeek(effectiveDue, selectedCycle);
            case FIXED_DATE -> nextDateByDateTokens(effectiveDue.plusDays(1), selectedCycle);
            case FIXED_MONTH -> {
                int anchor = effectiveDue.getDayOfMonth();
                LocalDate candidate = findNextSelectedMonth(effectiveDue.plusDays(1), selectedCycle);
                yield candidate.withDayOfMonth(Math.min(anchor, candidate.lengthOfMonth()));
            }
        };
    }

    private static LocalDate min(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? b : a;
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
                LocalDate d = cand.withDayOfMonth(1);
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
