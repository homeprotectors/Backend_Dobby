package com.homeprotectors.backend.repository;

import com.homeprotectors.backend.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByGroupId(Long groupId);
    Optional<Stock> findByIdAndGroupId(Long id, Long groupId);
}
