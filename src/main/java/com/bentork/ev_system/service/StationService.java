package com.bentork.ev_system.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.bentork.ev_system.dto.request.StationDTO;
import com.bentork.ev_system.mapper.StationMapper;
import com.bentork.ev_system.model.Charger;
import com.bentork.ev_system.model.Location;
import com.bentork.ev_system.model.Session;
import com.bentork.ev_system.model.Station;
import com.bentork.ev_system.repository.ChargerRepository;
import com.bentork.ev_system.repository.LocationRepository;
import com.bentork.ev_system.repository.StationRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private Clock clock;

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

    // Total Stations
    public Long getTotalStations() {
        return stationRepository.count();
    }

    // Active Stations
    public Long getActiveStations() {
        return stationRepository.findAll().stream()
                .filter(station -> "ACTIVE".equalsIgnoreCase(station.getStatus()))
                .count();
    }

    // Average Uptime (76%)
    public Double getAverageUptime() {
        List<Station> stations = stationRepository.findAll();

        if (stations.isEmpty()) {
            return 0.0;
        }

        double totalUptime = 0.0;
        int stationCount = 0;

        for (Station station : stations) {
            List<Charger> chargers = chargerRepository.findByStationId(station.getId());

            if (!chargers.isEmpty()) {
                long availableChargers = chargers.stream()
                        .filter(charger -> Boolean.TRUE.equals(charger.isAvailability()))
                        .count();

                double stationUptime = (availableChargers * 100.0) / chargers.size();
                totalUptime += stationUptime;
                stationCount++;
            }
        }

        double avgUptime = stationCount > 0 ? totalUptime / stationCount : 0.0;
        return Math.round(avgUptime * 100.0) / 100.0; // Round to 2 decimals
    }

    //Error Today
    public Long getTodaysErrorCount(){
        try {
            LocalDate today = LocalDate.now(clock);
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59, 999999999);

            log.debug("Getting all station to count todays errors");
            List<Station> allStations = stationRepository.findAll();

            return allStations.stream()
                    .filter(station -> station.getCreatedAt() != null
                    && (station.getCreatedAt().isEqual(startOfDay)
                    || (station.getCreatedAt().isAfter(startOfDay)
                    && station.getCreatedAt().isBefore(endOfDay))))
                    .filter(station -> station.getStatus() != null
                    && station.getStatus().toLowerCase().contains("error"))
                    .count();

        } catch (DataAccessException e) {
            log.error("Error while accessing data: {}", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in getTodaysErrorCount ", e);
            throw new RuntimeException("Failed to calculate today's error count", e);
        }
    }
}
