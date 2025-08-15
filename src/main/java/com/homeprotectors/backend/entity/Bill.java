package com.homeprotectors.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(value = 1, message = "청구 일자는 1일 이상이어야 합니다.")
    @Max(value = 31, message = "청구 일자는 31일 이하이어야 합니다.")
    private Integer dueDate; // 청구 일자 (일 단위로 입력, 예: 30)

    @Column(name = "is_variable")
    private Boolean isVariable; // 변동 청구서인지 여부 (기본 false)

    @Column(name = "isPaid")
    private Boolean isPaid; // 지불했는지

    @Column(name = "created_by", nullable = false)
    private Long createdBy; // 생성자 ID

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt = LocalDate.now(); // 생성 날짜

    @Column(name = "reminder_days")
    private Integer reminderDays; // 미리 알림 일수 (일 단위, 선택적, 기본값은 null)

    @Column(name = "reminder_date")
    private LocalDate reminderDate; // 미리 알림 날짜 (선택적, 기본값은 null)

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<BillHistory> billHistories = new java.util.ArrayList<>(); // 청구서 결제 기록들

}
