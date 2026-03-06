package com.homeprotectors.backend.repository;

import com.homeprotectors.backend.entity.Chore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface ChoreRepository extends JpaRepository<Chore, Long> {

    List<Chore> findByGroupId(Long groupId);

    // 오버듀 + 이번주~다음달 범위를 한 번에 조회
    @Query("""
        select c from Chore c
        where c.groupId = :groupId
          and c.nextDue is not null
          and (
               c.nextDue < :today
            or c.nextDue between :from and :to
          )
        order by c.nextDue asc, c.createdAt asc, c.id asc
    """)
    List<Chore> findByGroupIdAndNextDueBetweenOrNextDueBefore(
            @Param("groupId") Long groupId,
            @Param("from") LocalDate from,      // thisWeekStart
            @Param("to") LocalDate to,          // nextMonthEnd
            @Param("today") LocalDate today
    );
}

