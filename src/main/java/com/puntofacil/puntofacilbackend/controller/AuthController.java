package com.puntofacil.puntofacilbackend.controller;

import com.puntofacil.puntofacilbackend.dto.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {

        // por ahora solo validamos que llegue
        return ResponseEntity.ok("Login OK para usuario: " + request.getUsuario());
    }
}