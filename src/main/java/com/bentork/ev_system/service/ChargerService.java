package com.bentork.ev_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bentork.ev_system.dto.request.ChargerDTO;
import com.bentork.ev_system.model.Charger;
import com.bentork.ev_system.model.Station;
import com.bentork.ev_system.repository.ChargerRepository;
import com.bentork.ev_system.repository.StationRepository;



@Service
public class ChargerService {

    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private StationRepository stationRepository;

    public String createCharger(ChargerDTO dto) {
        Station station = stationRepository.findById(dto.getStationId())
                .orElseThrow(() -> new RuntimeException("Station not found"));

        Charger charger = new Charger();
        charger.setStation(station);
        charger.setOcppId(dto.getOcppId());
        charger.setConnectorType(dto.getConnectorType());
        charger.setChargerType(dto.getChargerType());
        charger.setRate(dto.getRate());
        charger.setOccupied(dto.isOccupied());
        charger.setAvailability(dto.isAvailability());

        chargerRepository.save(charger);

        return "Charger Created";
    }


    public List<ChargerDTO> getAllChargers() {
        return chargerRepository.findAll().stream().map(charger -> {
            ChargerDTO dto = new ChargerDTO();
            dto.setId(charger.getId());
            dto.setStationId(charger.getStation().getId());
            dto.setStationName(charger.getStation().getName());
            dto.setOcppId(charger.getOcppId());
            dto.setConnectorType(charger.getConnectorType());
            dto.setChargerType(charger.getChargerType());
            dto.setRate(charger.getRate());
            dto.setOccupied(charger.isOccupied());
            dto.setAvailability(charger.isAvailability());
            dto.setCreatedAt(charger.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public ChargerDTO getChargerById(Long id) {
        Charger charger = chargerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charger not found"));

        ChargerDTO dto = new ChargerDTO();
        dto.setId(charger.getId());
        dto.setStationId(charger.getStation().getId());
        dto.setStationName(charger.getStation().getName());
        dto.setOcppId(charger.getOcppId());
        dto.setConnectorType(charger.getConnectorType());
        dto.setChargerType(charger.getChargerType());
        dto.setRate(charger.getRate());
        dto.setOccupied(charger.isOccupied());
        dto.setAvailability(charger.isAvailability());
        dto.setCreatedAt(charger.getCreatedAt());

        return dto;
    }

    public String updateCharger(Long id, ChargerDTO dto) {
        Charger charger = chargerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charger not found"));

        Station station = stationRepository.findById(dto.getStationId())
                .orElseThrow(() -> new RuntimeException("Station not found"));

        charger.setStation(station);
        charger.setOcppId(dto.getOcppId());
        charger.setConnectorType(dto.getConnectorType());
        charger.setChargerType(dto.getChargerType());
        charger.setRate(dto.getRate());
        charger.setOccupied(dto.isOccupied());
        charger.setAvailability(dto.isAvailability());

        chargerRepository.save(charger);
        return "Charger Updated";
    }

    public String deleteCharger(Long id) {
        if (!chargerRepository.existsById(id)) {
            throw new RuntimeException("Charger not found");
        }
        chargerRepository.deleteById(id);
        return "Charger Deleted";
    }
}

