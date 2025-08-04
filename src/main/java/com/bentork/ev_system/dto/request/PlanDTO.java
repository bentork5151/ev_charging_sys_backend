package com.bentork.ev_system.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PlanDTO {
    private Long id;
    private String planName;
    private String description;
    private Integer durationMin;
    private BigDecimal walletDeduction;
    private String chargerType;
    private BigDecimal rate;
    private Long createdBy; // for response purposes only
}
