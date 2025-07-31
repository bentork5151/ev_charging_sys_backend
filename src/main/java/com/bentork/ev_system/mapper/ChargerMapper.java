package com.bentork.ev_system.mapper;

import com.bentork.ev_system.dto.request.ChargerDTO;
import com.bentork.ev_system.model.Charger;

public class ChargerMapper {

    public static ChargerDTO toDto(Charger charger) {
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

    public static Charger toEntity(ChargerDTO dto) {
        Charger charger = new Charger();
        charger.setOcppId(dto.getOcppId());
        charger.setConnectorType(dto.getConnectorType());
        charger.setChargerType(dto.getChargerType());
        charger.setRate(dto.getRate());
        charger.setOccupied(dto.isOccupied());
        charger.setAvailability(dto.isAvailability());
        return charger;
    }
}
