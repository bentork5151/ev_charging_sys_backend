package com.bentork.ev_system.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bentork.ev_system.model.Session;
import com.bentork.ev_system.service.RFIDChargingService;

@RestController
@RequestMapping("/api/rfid")
public class RFIDChargingController {

    @Autowired
    private RFIDChargingService chargingService;

    @PostMapping("/start")
    public ResponseEntity<Session> start(@RequestBody Map<String, Object> req) {
        String cardNumber = req.get("cardNumber").toString();
        Long chargerId = Long.valueOf(req.get("chargerId").toString());
        String boxId = req.get("boxId").toString();
        return ResponseEntity.ok(chargingService.startCharging(cardNumber,
                chargerId, boxId));
    }

    @PostMapping("/update-energy")
    public ResponseEntity<Session> updateEnergy(@RequestBody Map<String, Object> req) {
        Long sessionId = Long.valueOf(req.get("sessionId").toString());
        BigDecimal currentKwh = new BigDecimal(req.get("currentKwh").toString());
        return ResponseEntity.ok(chargingService.updateEnergy(sessionId,
                currentKwh));
    }

    @PostMapping("/stop/{sessionId}")
    public ResponseEntity<Session> stop(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chargingService.stopCharging(sessionId));
    }
}
