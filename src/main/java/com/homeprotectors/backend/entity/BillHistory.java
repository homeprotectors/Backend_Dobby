package com.homeprotectors.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Entity
@Table(name = "bill_history",
        indexes = {
                @Index(name = "idx_history_group_month", columnList = "groupId, yearMonth"),
                @Index(name = "idx_history_bill", columnList = "bill_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_group_bill_month", columnNames = {"groupId", "bill_id", "yearMonth"})
        })
@Getter @Setter
public class BillHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;

    @Column
    private Long paidBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    /**
     * 해당 월의 '첫째 날'로 저장 (예: 2025-07-01)
     */
    @Column(nullable = false)
    private LocalDate yearMonth;

    @Column(nullable = false)
    private Integer amount;      // 그 달 실제 납부액 (변동 bill)

    @Column
    private LocalDate paidDate;  // 선택(입력했을 때만)

}
