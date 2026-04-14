package com.puntofacil.puntofacilbackend.controller;

import com.puntofacil.puntofacilbackend.entity.Cliente;
import com.puntofacil.puntofacilbackend.service.ClienteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    /**
     * BUSCADOR PARA EL POS (Autocompletado)
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarClientes(
            @RequestParam(value = "term", required = false, defaultValue = "") String term,
            HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            return ResponseEntity.ok(clienteService.buscarClientes(idEmpresa, term));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * OBTENER TODOS LOS ACTIVOS DE LA EMPRESA
     */
    @GetMapping("/activos")
    public ResponseEntity<?> listarActivos(HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            return ResponseEntity.ok(clienteService.listarActivos(idEmpresa));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GUARDAR O EDITAR CLIENTE
     */
    @PostMapping("/guardar")
    public ResponseEntity<?> guardar(@RequestBody Cliente cliente, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            // Inyectamos el ID de empresa desde la sesión para evitar suplantación
            Cliente guardado = clienteService.guardar(cliente, idEmpresa);
            return ResponseEntity.ok(guardado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo procesar la solicitud", "detalle", e.getMessage()));
        }
    }

    /**
     * OBTENER UN CLIENTE ESPECÍFICO
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            return clienteService.obtenerPorIdYEmpresa(id, idEmpresa)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DESACTIVACIÓN LÓGICA
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            boolean desactivado = clienteService.desactivarCliente(id, idEmpresa);
            return desactivado ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    // --- MÉTODOS DE SOPORTE PARA SESIÓN (Sincronizado con SuccessHandler) ---

    private Integer getEmpresaId(HttpSession session) {
        // Se eliminó la búsqueda de "id_empresa" para mantener el estándar "idEmpresa"
        Integer id = (Integer) session.getAttribute("idEmpresa");

        if (id == null) {
            throw new RuntimeException("Acceso denegado: Sesión de empresa no encontrada.");
        }
        return id;
    }
}