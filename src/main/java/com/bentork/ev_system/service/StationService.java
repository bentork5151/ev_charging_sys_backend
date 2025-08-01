package com.bentork.ev_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bentork.ev_system.dto.request.StationDTO;
import com.bentork.ev_system.mapper.StationMapper;
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

    public StationDTO createStation(StationDTO dto) {
        Location location = locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));

        Station station = StationMapper.toEntity(dto);
        station.setLocation(location);

        Station saved = stationRepository.save(station);
        return StationMapper.toDTO(saved);
    }

    public List<StationDTO> getAllStations() {
        return stationRepository.findAll().stream()
                .map(StationMapper::toDTO)
                .collect(Collectors.toList());
    }

    public StationDTO getStationById(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with ID: " + id));
        return StationMapper.toDTO(station);
    }

    public StationDTO updateStation(Long id, StationDTO dto) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with ID: " + id));

        Location location = locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));

        station.setName(dto.getName());
        station.setLocation(location);
        station.setStatus(dto.getStatus());
        station.setDirectionLink(dto.getDirectionLink());

        Station updated = stationRepository.save(station);
        return StationMapper.toDTO(updated);
    }

    public void deleteStation(Long id) {
        if (!stationRepository.existsById(id)) {
            throw new EntityNotFoundException("Station not found with ID: " + id);
        }
        stationRepository.deleteById(id);
    }
}
