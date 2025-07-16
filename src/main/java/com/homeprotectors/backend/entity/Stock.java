package com.homeprotectors.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Data
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 그룹 ID (재고가 속한 그룹)
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private String name; // 재고 이름

    // 0 이상, 1000 이하의 수량을 허용
    @Column(nullable = false)
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    @Max(value = 1000, message = "재고 수량은 1000 이하이어야 합니다.")
    private Integer unitQuantity; // 재고 수량

    @Column(nullable = false)
    private String unit; // 재고 단위 (예: 병, 개, 박스 등)

    @Column(name = "unit_days", nullable = false)
    @Min(value = 1, message = "예상 소진 일수는 1일 이상이어야 합니다.")
    private Integer unitDays; // 예상 소진 일수

    @Column(name = "next_due")
    private LocalDate nextDue; // 다음 소진 예정일

    @Column(name = "created_by", nullable = false)
    private Long createdBy; // 생성자 ID

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now(); // 마지막 업데이트 시간

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성 시간

    @Column(name = "reminder_date")
    private LocalDate reminderDate; // 미리 알림 날짜

    @Column(name = "reminder_days")
    @Min(value = 0, message = "미리 알림 일수는 0일 이상이어야 합니다.")
    private Integer reminderDays; // 미리 알림 일수 (0일 이상)

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<StockHistory> stockHistories = new java.util.ArrayList<>(); // 재고 이력
}
