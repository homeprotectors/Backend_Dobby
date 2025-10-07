package com.homeprotectors.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Enumerated(EnumType.STRING)
    private RecurrenceType recurrenceType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "chore_selected_cycle", joinColumns = @JoinColumn(name = "chore_id"))
    @Column(name = "value")
    private Set<String> selectedCycle = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private RoomCategory roomCategory; // 방 카테고리

    private LocalDate nextDue;

    private Long createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "chore", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ChoreHistory> choreHistories = new java.util.ArrayList<>();

//  com.homeprotectors.backend.entity.Group Entity 우선 주석처리 - 향후 기능 확장
//    @ManyToOne
//    @JoinColumn(name = "group_id")
//    private com.homeprotectors.backend.entity.Group group;
}
