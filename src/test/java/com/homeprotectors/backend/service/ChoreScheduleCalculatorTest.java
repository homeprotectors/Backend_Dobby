package com.homeprotectors.backend.service;

import com.homeprotectors.backend.entity.RecurrenceType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChoreScheduleCalculatorTest {

    private final ChoreScheduleCalculator calculator = new ChoreScheduleCalculator();

    @Test
    void fixedDayEarlyCompletionMovesToFollowingOccurrence() {
        LocalDate nextDue = calculator.calculateNextDue(
                RecurrenceType.FIXED_DAY,
                Set.of("SUNDAY"),
                LocalDate.of(2026, 4, 11),
                LocalDate.of(2026, 4, 12)
        );

        assertEquals(LocalDate.of(2026, 4, 19), nextDue);
    }

    @Test
    void fixedDayOnDueDateMovesOneWeekLater() {
        LocalDate nextDue = calculator.calculateNextDue(
                RecurrenceType.FIXED_DAY,
                Set.of("SUNDAY"),
                LocalDate.of(2026, 4, 12),
                LocalDate.of(2026, 4, 12)
        );

        assertEquals(LocalDate.of(2026, 4, 19), nextDue);
    }

    @Test
    void intervalEarlyCompletionUsesCurrentDueAsAnchor() {
        LocalDate nextDue = calculator.calculateNextDue(
                RecurrenceType.PER_WEEK,
                Set.of(),
                LocalDate.of(2026, 4, 11),
                LocalDate.of(2026, 4, 12)
        );

        assertEquals(LocalDate.of(2026, 4, 19), nextDue);
    }

    @Test
    void intervalLateCompletionUsesDoneDateAsAnchor() {
        LocalDate nextDue = calculator.calculateNextDue(
                RecurrenceType.PER_WEEK,
                Set.of(),
                LocalDate.of(2026, 4, 14),
                LocalDate.of(2026, 4, 12)
        );

        assertEquals(LocalDate.of(2026, 4, 21), nextDue);
    }

    @Test
    void fixedDateEarlyCompletionMovesToNextConfiguredDate() {
        LocalDate nextDue = calculator.calculateNextDue(
                RecurrenceType.FIXED_DATE,
                Set.of("5"),
                LocalDate.of(2026, 4, 3),
                LocalDate.of(2026, 4, 5)
        );

        assertEquals(LocalDate.of(2026, 5, 5), nextDue);
    }
}
