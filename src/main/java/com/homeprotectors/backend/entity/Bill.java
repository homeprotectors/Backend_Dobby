package com.homeprotectors.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table
@Data
public class Bill {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId; // 그룹 ID (청구서가 속한 그룹)

    @Column(nullable = false)
    private String name; // 청구서 제목

    @Column(nullable = false)
    private Double amount; // 청구 금액

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "isPaid")
    private Boolean isPaid; // 지불했는지

    @Column(name = "created_by", nullable = false)
    private Long createdBy; // 생성자 ID

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt = LocalDate.now(); // 생성 날짜

}
