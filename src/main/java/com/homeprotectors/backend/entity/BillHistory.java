package com.homeprotectors.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table
@Data
public class BillHistory {

    @Id
    @GeneratedValue
    private Long id; // 결제 기록 ID

    @ManyToOne
    @JoinColumn(name = "bill_id", nullable = false)
    private Long billId; // 청구서 ID (어떤 청구서의 결제 기록인지)

    @Column(name = "paid_date", nullable = false)
    private java.time.LocalDate paidDate; // 결제 날짜

    @Column(name = "paid_amount", nullable = false)
    private Double paidAmount; // 결제 금액

    @Column(name = "paid_by", nullable = false)
    private Long paidBy; // 결제한 사람의 ID

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
