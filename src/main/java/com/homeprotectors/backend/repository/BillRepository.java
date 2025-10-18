package com.homeprotectors.backend.repository;

import com.homeprotectors.backend.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByGroupId(Long groupId); // @Where 로 soft-deleted 제외됨

    Optional<Bill> findByIdAndGroupId(Long id, Long groupId);

    long countByGroupId(Long groupId);
}
