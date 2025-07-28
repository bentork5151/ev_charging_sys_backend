package com.bentork.ev_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bentork.ev_system.dto.request.StationDTO;
import com.bentork.ev_system.model.Location;
import com.bentork.ev_system.model.Station;
import com.bentork.ev_system.repository.LocationRepository;
import com.bentork.ev_system.repository.StationRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private LocationRepository locationRepository;

    /**
     * Create a new station
     */
    public StationDTO createStation(StationDTO dto) {
        Location location = locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));

        Station station = new Station();
        station.setLocation(location);
        station.setName(dto.getName());
        station.setOcppId(dto.getOcppId());
        station.setType(dto.getType());
        station.setStatus(dto.getStatus());
        station.setDirectionLink(dto.getDirectionLink());

        Station saved = stationRepository.save(station);

        return mapToDTO(saved);
    }

    /**
     * Get all stations
     */
    public List<StationDTO> getAllStations() {
        return stationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get station by ID
     */
    public StationDTO getStationById(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with ID: " + id));
        return mapToDTO(station);
    }

    /**
     * Update station
     */
    public StationDTO updateStation(Long id, StationDTO dto) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with ID: " + id));

        Location location = locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));

        station.setLocation(location);
        station.setName(dto.getName());
        station.setOcppId(dto.getOcppId());
        station.setType(dto.getType());
        station.setStatus(dto.getStatus());
        station.setDirectionLink(dto.getDirectionLink());

        Station updated = stationRepository.save(station);

        return mapToDTO(updated);
    }

    /**
     * Delete station by ID
     */
    public void deleteStation(Long id) {
        if (!stationRepository.existsById(id)) {
            throw new EntityNotFoundException("Station not found with ID: " + id);
        }
        stationRepository.deleteById(id);
    }

    /**
     * Map Station entity to DTO
     */
    private StationDTO mapToDTO(Station station) {
        StationDTO dto = new StationDTO();
        dto.setId(station.getId());
        dto.setLocationId(station.getLocation().getId());
        dto.setLocationName(station.getLocation().getName());
        dto.setName(station.getName());
        dto.setOcppId(station.getOcppId());
        dto.setType(station.getType());
        dto.setStatus(station.getStatus());
        dto.setDirectionLink(station.getDirectionLink());
        dto.setCreatedAt(station.getCreatedAt());
        return dto;
    }
}
