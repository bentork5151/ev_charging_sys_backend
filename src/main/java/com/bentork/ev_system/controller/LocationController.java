package com.bentork.ev_system.controller;

import com.bentork.ev_system.dto.request.LocationDTO;
import com.bentork.ev_system.mapper.LocationMapper;
import com.bentork.ev_system.model.Admin;
import com.bentork.ev_system.model.Location;
import com.bentork.ev_system.repository.AdminRepository;
import com.bentork.ev_system.repository.LocationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.springframework.web.bind.annotation.GetMapping;

import com.bentork.ev_system.service.LocationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/location")
@PreAuthorize("hasAuthority('ADMIN')")
public class LocationController {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private LocationService locationService;

    // ✅ Add location
    @PostMapping("/add")
    public ResponseEntity<?> addLocation(@RequestBody LocationDTO dto, Authentication authentication) {
        String adminEmail = authentication.getName();
        Optional<Admin> admin = adminRepository.findByEmail(adminEmail);
        if (admin.isEmpty()) {
            return ResponseEntity.status(403).body(Collections.singletonMap("error", "Admin not found"));
        }

        Location location = LocationMapper.toEntity(dto, admin.get());
        locationRepository.save(location);
        return ResponseEntity.ok(location);
    }

    // ✅ Get all locations
    @GetMapping("/all")
    public ResponseEntity<?> getAllLocations() {
        try {
            List<Location> locations = locationRepository.findAll();
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            e.printStackTrace(); // Important for logs
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch locations"));
        }
    }

    // ✅ Get location by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getLocationById(@PathVariable Long id) {
        Optional<Location> location = locationRepository.findById(id);

        if (location.isPresent()) {
            return ResponseEntity.ok(location.get());
        } else {
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "Location not found"));
        }
    }

    // ✅ Update location
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateLocation(@PathVariable Long id, @RequestBody LocationDTO updatedDto, Authentication authentication) {
        Optional<Location> optionalLocation = locationRepository.findById(id);
        if (optionalLocation.isEmpty()) {
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "Location not found"));
        }

        String adminEmail = authentication.getName();
        Optional<Admin> admin = adminRepository.findByEmail(adminEmail);
        if (admin.isEmpty()) {
            return ResponseEntity.status(403).body(Collections.singletonMap("error", "Admin not found"));
        }

        Location updated = LocationMapper.updateEntity(optionalLocation.get(), updatedDto, admin.get());
        locationRepository.save(updated);
        return ResponseEntity.ok(Collections.singletonMap("message", "Location Updated"));
    }

    // ✅ Delete location
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteLocation(@PathVariable Long id) {
        if (locationRepository.existsById(id)) {
            locationRepository.deleteById(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "Location Deleted"));
        } else {
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "Location not found"));
        }
    }

    @GetMapping("/all/name")
    public ResponseEntity<?> getAllLocationNames() {
        try {
            log.info("Calling Location service to get all location name and id.");
            List<Map<String, Object>> location = locationService.getAllLocationNames();
            return ResponseEntity.ok(location);
        } catch (Exception e) {
            log.error("Fail to get location name and id: {}", e);
            return ResponseEntity.internalServerError().body("Error Fetching location info");
        }
    }

}
