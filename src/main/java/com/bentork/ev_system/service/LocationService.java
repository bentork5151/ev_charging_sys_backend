package com.bentork.ev_system.service;

import com.bentork.ev_system.dto.request.LocationDTO;
import com.bentork.ev_system.mapper.LocationMapper;
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

    public Location addLocation(LocationDTO dto, Admin admin) {
        Location location = LocationMapper.toEntity(dto, admin);
        return locationRepo.save(location);
    }

}


