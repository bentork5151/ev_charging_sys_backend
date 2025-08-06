package com.bentork.ev_system.controller;

import com.bentork.ev_system.dto.request.EmergencyContactDTO;
import com.bentork.ev_system.service.EmergencyContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergency-contacts")
public class EmergencyContactController {

    @Autowired
    private EmergencyContactService contactService;

    @PostMapping("/add")
    public ResponseEntity<String> create(@RequestBody EmergencyContactDTO dto) {
        contactService.createEmergencyContact(dto);
        return ResponseEntity.ok("Contact Created");
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmergencyContactDTO>> getAll() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            EmergencyContactDTO dto = contactService.getEmergencyContactById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody EmergencyContactDTO dto) {
        contactService.updateContact(id, dto);
        return ResponseEntity.ok("Contact Updated");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok("Contact Deleted");
    }
}
