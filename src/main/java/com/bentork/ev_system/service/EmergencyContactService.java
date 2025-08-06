package com.bentork.ev_system.service;

import com.bentork.ev_system.dto.request.EmergencyContactDTO;
import com.bentork.ev_system.mapper.EmergencyContactMapper;
import com.bentork.ev_system.model.EmergencyContact;
import com.bentork.ev_system.model.Station;
import com.bentork.ev_system.repository.EmergencyContactRepository;
import com.bentork.ev_system.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmergencyContactService {

    @Autowired
    private EmergencyContactRepository contactRepository;

    @Autowired
    private StationRepository stationRepository;

    public void createEmergencyContact(EmergencyContactDTO dto) {
        Station station = stationRepository.findById(dto.getStationId())
                .orElseThrow(() -> new RuntimeException("Station not found"));
        EmergencyContact contact = EmergencyContactMapper.toEntity(dto, station);
        contactRepository.save(contact);
    }

    public List<EmergencyContactDTO> getAllContacts() {
        return contactRepository.findAll().stream()
                .map(EmergencyContactMapper::toDTO)
                .collect(Collectors.toList());
    }

    public EmergencyContactDTO getEmergencyContactById(Long id) {
        EmergencyContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        return EmergencyContactMapper.toDTO(contact);
    }

    public void updateContact(Long id, EmergencyContactDTO dto) {
        EmergencyContact existing = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        Station station = stationRepository.findById(dto.getStationId())
                .orElseThrow(() -> new RuntimeException("Station not found"));

        EmergencyContact updated = EmergencyContactMapper.toEntity(dto, station);
        updated.setId(id);
        contactRepository.save(updated);
    }

    public void deleteContact(Long id) {
        if (!contactRepository.existsById(id)) {
            throw new RuntimeException("Contact not found");
        }
        contactRepository.deleteById(id);
    }
}
