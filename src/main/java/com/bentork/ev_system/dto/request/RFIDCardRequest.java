package com.bentork.ev_system.dto.request;

import lombok.Data;

@Data
public class RFIDCardRequest {
    private Long userId;
    private String cardNumber;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
