package com.bentork.ev_system.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String planName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer durationMin;

    private BigDecimal walletDeduction;

    private String chargerType; // AC or DC

    private BigDecimal rate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Admin createdBy;
}
