package com.homeprotectors.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "bills", indexes = {
        @Index(name = "idx_bills_group", columnList = "groupId")
})
@Getter
@Setter
@SQLDelete(sql = "UPDATE bills SET deleted_at = now() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)  // 멀티테넌시(그룹 구분)
    private Long groupId;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillCategory category; // 주거/생활취미/기타

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillType type;         // 수도세/전기세/가스난방/관리비/기타

    @Column(nullable = false)
    private Double amount;        // 원화 정수(필요시 BigDecimal로 교체)

    @Column(nullable = false)
    private Boolean isVariable;    // true=변동, false=고정

    @Column
    private Integer dueDate;       // 1~31 or null(옵셔널)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 소프트 삭제 마킹

}
