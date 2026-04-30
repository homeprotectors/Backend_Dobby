package com.homeprotectors.backend.repository;

import com.homeprotectors.backend.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByGroupId(Long groupId); // @Where 로 soft-deleted 제외됨

    Optional<Bill> findByIdAndGroupId(Long id, Long groupId);

    long countByGroupId(Long groupId);

    @Modifying
    @Query("delete from Bill b where b.groupId = :groupId")
    void hardDeleteByGroupId(Long groupId);
}
