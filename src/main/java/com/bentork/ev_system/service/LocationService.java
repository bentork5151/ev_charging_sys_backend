package com.bentork.ev_system.service;

import com.bentork.ev_system.dto.request.LocationDTO;
import com.bentork.ev_system.model.Admin;
import com.bentork.ev_system.model.Location;
import com.bentork.ev_system.repository.AdminRepository;
import com.bentork.ev_system.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepo;

    @Autowired
    private AdminRepository adminRepo;

    public Location addLocation(LocationDTO dto, String adminEmail) {
        Admin admin = adminRepo.findByEmail(adminEmail)
                               .orElseThrow(() -> new RuntimeException("Admin not found"));

        Location location = new Location();
        location.setName(dto.getName());
        location.setAddress(dto.getAddress());
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setCity(dto.getCity());
        location.setState(dto.getState());
        location.setCreatedBy(admin);

        return locationRepo.save(location);
    }
}


