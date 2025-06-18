package com.homeprotectors.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;
import org.hibernate.validator.constraints.UniqueElements;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "chore_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chore_id", nullable = false)
    private Chore chore;

    @Column(nullable = false)
    private LocalDate scheduledDate; // 예정 추천일

    private LocalDate doneDate;       // 실제 완료한 날짜 (완료한 경우만)

    @Column(nullable = false)
    private Long doneBy; // 완료한 사람

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
