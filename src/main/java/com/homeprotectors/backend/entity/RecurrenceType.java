package com.homeprotectors.backend.entity;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public enum RecurrenceType {
    PER_WEEK(Set.of()),
    PER_2WEEKS(Set.of()),
    PER_MONTH(Set.of()),
    FIXED_DAY(Set.of("MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY")),
    // DATE는 숫자 1~30, END 허용
    FIXED_DATE(Stream.concat(
            Stream.of("END"),
            IntStream.rangeClosed(1, 30).mapToObj(String::valueOf)
    ).collect(Collectors.toSet())),
    // MONTH는 숫자 1~12 허용
    FIXED_MONTH(Set.of(
            IntStream.rangeClosed(1, 12).mapToObj(String::valueOf).toArray(String[]::new)
    ));

    private final Set<String> allowedValues;

    RecurrenceType(Set<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public boolean isValidSelection(Set<String> selected) {
        if (allowedValues.isEmpty()) {
            return selected == null || selected.isEmpty();
        }
        return selected != null && !selected.isEmpty()
                && selected.stream().allMatch(allowedValues::contains);
    }
}
