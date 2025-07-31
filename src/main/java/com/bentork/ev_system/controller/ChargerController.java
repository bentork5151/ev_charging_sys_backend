package com.bentork.ev_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bentork.ev_system.dto.request.ChargerDTO;
import com.bentork.ev_system.service.ChargerService;

@PreAuthorize("hasAuthority('ADMIN')")
@RestController
@RequestMapping("/api/chargers")
public class ChargerController {

    private final ChargerService chargerService;
	
	public ChargerController(ChargerService chargerService) {
        this.chargerService = chargerService;
    }


	@PostMapping("/add")
	public ResponseEntity<String> createCharger(@RequestBody ChargerDTO dto) {
	    return ResponseEntity.ok(chargerService.createCharger(dto));
	}

    @GetMapping("/all")
    public ResponseEntity<List<ChargerDTO>> getAllChargers() {
        return ResponseEntity.ok(chargerService.getAllChargers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChargerDTO> getChargerById(@PathVariable Long id) {
        return ResponseEntity.ok(chargerService.getChargerById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateCharger(@PathVariable Long id, @RequestBody ChargerDTO dto) {
        return ResponseEntity.ok(chargerService.updateCharger(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCharger(@PathVariable Long id) {
        return ResponseEntity.ok(chargerService.deleteCharger(id));
    }

}
