package com.homeprotectors.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock; // 재고 엔티티와의 연관 관계

    @Column(nullable = false)
    private Integer refilledQuantity;

    @Column(nullable = false)
    private Long refilledBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
