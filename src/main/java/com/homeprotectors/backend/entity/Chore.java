package com.homeprotectors.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "chores")
@Data
public class Chore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private String title;

    private Integer cycleDays;
    private LocalDate startDate;
    private LocalDate lastDone;
    private LocalDate nextDue;
    private LocalDate reminderDate;
    @Setter
    @Getter
    private Boolean reminderEnabled = true;
    private Integer reminderDays;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

//  com.homeprotectors.backend.entity.Group Entity 우선 주석처리 - 향후 기능 확장
//    @ManyToOne
//    @JoinColumn(name = "group_id")
//    private com.homeprotectors.backend.entity.Group group;
}
