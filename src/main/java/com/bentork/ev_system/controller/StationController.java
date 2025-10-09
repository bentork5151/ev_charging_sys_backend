package com.bentork.ev_system.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bentork.ev_system.dto.request.StationDTO;

import com.bentork.ev_system.service.StationService;

@PreAuthorize("hasAuthority('ADMIN')")
@RestController
@RequestMapping("/api/stations")
public class StationController {

    @Autowired
    private StationService stationService;

    // @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<String> createStation(@RequestBody StationDTO dto) {
        stationService.createStation(dto);
        return ResponseEntity.ok("Station Created");
    }

    @GetMapping("/all")
    public ResponseEntity<List<StationDTO>> getAllStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationDTO> getStationById(@PathVariable Long id) {
        return ResponseEntity.ok(stationService.getStationById(id));
    }

    // @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateStation(@PathVariable Long id, @RequestBody StationDTO dto) {
        stationService.updateStation(id, dto);
        return ResponseEntity.ok("Station Updated");
    }

    // @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteStation(@PathVariable Long id) {
        stationService.deleteStation(id);
        return ResponseEntity.ok("Station Deleted");
    }

    // Total Stations
    @GetMapping("/total")
    public ResponseEntity<Long> getTotalStations(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(stationService.getTotalStations());
    }

    // Active Stations
    @GetMapping("/active")
    public ResponseEntity<Long> getActiveStations(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(stationService.getActiveStations());
    }

    // Average Uptime
    @GetMapping("/uptime")
    public ResponseEntity<Double> getAverageUptime(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(stationService.getAverageUptime());
    }
}
