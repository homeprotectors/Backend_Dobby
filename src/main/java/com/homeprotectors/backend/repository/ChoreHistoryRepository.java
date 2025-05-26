package com.homeprotectors.backend.repository;

import com.homeprotectors.backend.entity.ChoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChoreHistoryRepository extends JpaRepository<ChoreHistory, Long> {
    List<ChoreHistory> findByChoreId(Long choreId);
}