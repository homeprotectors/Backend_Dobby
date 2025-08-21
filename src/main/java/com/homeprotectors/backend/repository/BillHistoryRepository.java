package com.homeprotectors.backend.repository;

import com.homeprotectors.backend.entity.BillHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillHistoryRepository extends JpaRepository<BillHistory, Long> {
}
