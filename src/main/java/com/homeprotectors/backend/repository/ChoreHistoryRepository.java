package com.homeprotectors.backend.repository;

import com.homeprotectors.backend.entity.ChoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChoreHistoryRepository extends JpaRepository<ChoreHistory, Long> {
    List<ChoreHistory> findByChoreId(Long choreId);
    Optional<ChoreHistory> findByChoreIdAndDoneDate(Long choreId, LocalDate doneDate);
    Optional<ChoreHistory> findTopByChoreIdOrderByDoneDateDesc(Long choreId);
    List<ChoreHistory> findByChoreIdOrderByDoneDateDesc(Long choreId);
    boolean existsByChoreIdAndDoneDate(Long id, LocalDate doneDate);
}