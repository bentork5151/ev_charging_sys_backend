package com.bentork.ev_system.controller;

import com.bentork.ev_system.dto.request.LocationDTO;
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

@RestController
@RequestMapping("/api/location")
@PreAuthorize("hasAuthority('ADMIN')")
public class LocationController {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private AdminRepository adminRepository;

    // ✅ Add location
    @PostMapping("/add")
    public ResponseEntity<?> addLocation(@RequestBody LocationDTO dto, Authentication authentication) {
        String adminEmail = authentication.getName();
        Optional<Admin> admin = adminRepository.findByEmail(adminEmail);
        if (admin.isEmpty()) {
            return ResponseEntity.status(403).body(Collections.singletonMap("error", "Admin not found"));
        }

        Location location = new Location();
        location.setName(dto.getName());
        location.setAddress(dto.getAddress());
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setCity(dto.getCity());
        location.setState(dto.getState());
        location.setCreatedBy(admin.get());

        locationRepository.save(location);
        return ResponseEntity.ok(Collections.singletonMap("message", "Location Added"));
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

        Location location = optionalLocation.get();
        location.setName(updatedDto.getName());
        location.setAddress(updatedDto.getAddress());
        location.setLatitude(updatedDto.getLatitude());
        location.setLongitude(updatedDto.getLongitude());
        location.setCity(updatedDto.getCity());
        location.setState(updatedDto.getState());
        location.setCreatedBy(admin.get());

        locationRepository.save(location);
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
}
