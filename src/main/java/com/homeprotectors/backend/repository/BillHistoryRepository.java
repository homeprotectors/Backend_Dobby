package com.homeprotectors.backend.repository;

import com.homeprotectors.backend.entity.BillHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillHistoryRepository extends JpaRepository<BillHistory, Long> {

    List<BillHistory> findByGroupIdAndYearMonth(Long groupId, LocalDate yearMonth);

    Optional<BillHistory> findByGroupIdAndBill_IdAndYearMonth(Long groupId, Long billId, LocalDate yearMonth);

    @Query("""
        select coalesce(sum(h.amount), 0)
        from BillHistory h
        where h.groupId = :groupId and h.yearMonth = :yearMonth
    """)
    Integer sumByGroupAndMonth(Long groupId, LocalDate yearMonth);
}
