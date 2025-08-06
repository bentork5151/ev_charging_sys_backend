package com.bentork.ev_system.dto.request;

public class SessionDTO {

        private Long userId;      // Added
        private Long chargerId;
        private String boxId;
        private Long sessionId;

        private String message;
        private double energyUsed;
        private double cost;
        private String status;

        // Getters and Setters

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getChargerId() {
            return chargerId;
        }

        public void setChargerId(Long chargerId) {
            this.chargerId = chargerId;
        }

        public String getBoxId() {
            return boxId;
        }

        public void setBoxId(String boxId) {
            this.boxId = boxId;
        }

        public Long getSessionId() {
            return sessionId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public double getEnergyUsed() {
            return energyUsed;
        }

        public void setEnergyUsed(double energyUsed) {
            this.energyUsed = energyUsed;
        }

        public double getCost() {
            return cost;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
}
