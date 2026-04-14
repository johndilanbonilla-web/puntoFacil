package com.puntofacil.puntofacilbackend.controller;

import com.puntofacil.puntofacilbackend.dto.VentaDTO;
import com.puntofacil.puntofacilbackend.entity.Venta;
import com.puntofacil.puntofacilbackend.service.VentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    /**
     * Carga la vista del historial con filtros y seguridad por empresa.
     */
    @GetMapping("/historial")
    public String verHistorial(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String cliente,
            HttpSession session,
            Model model) {

        try {
            Integer idEmpresa = getEmpresaId(session);
            // Sincronizado con la nueva lógica de Service
            List<Venta> ventas = ventaService.buscarConFiltros(idEmpresa, fechaInicio, fechaFin, cliente);
            model.addAttribute("ventas", ventas);
            return "ventas/lista";
        } catch (RuntimeException e) {
            return "redirect:/login?error=session_expired";
        }
    }

    /**
     * Obtiene productos favoritos para la pantalla rápida del POS.
     */
    @GetMapping("/favoritos")
    @ResponseBody
    public ResponseEntity<?> obtenerFavoritos(HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            return ResponseEntity.ok(ventaService.obtenerFavoritos(idEmpresa));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtiene los 10 productos más vendidos de la semana.
     */
    @GetMapping("/mas-vendidos")
    @ResponseBody
    public ResponseEntity<?> obtenerMasVendidos(HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            // El límite de 10 se controla internamente en el Service/SQL
            return ResponseEntity.ok(ventaService.obtenerMasVendidosSemana(idEmpresa));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Procesa la creación de una nueva venta.
     */
    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<?> crearVenta(@RequestBody VentaDTO ventaDto, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer idEmpresa = getEmpresaId(session);
            Integer idUsuarioActual = getUsuarioId(session);

            ventaDto.setIdEmpresa(idEmpresa);
            ventaDto.setIdSucursal(getSucursalId(session));

            Venta ventaGuardada = ventaService.guardarVenta(ventaDto, idUsuarioActual, idEmpresa);

            response.put("status", "success");
            response.put("idVenta", ventaGuardada.getIdVenta());
            response.put("mensaje", "¡Venta #" + ventaGuardada.getIdVenta() + " realizada!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("mensaje", "Error al procesar: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Anulación segura de ventas con retorno de stock y ajuste de caja.
     */
    @PostMapping("/anular/{id}")
    @ResponseBody
    public ResponseEntity<?> anularVenta(@PathVariable("id") Integer id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer idEmpresa = getEmpresaId(session);
            ventaService.anularVentaSegura(id, idEmpresa);

            response.put("status", "success");
            response.put("mensaje", "Venta anulada correctamente.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("mensaje", "Error al anular: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // --- MÉTODOS DE SOPORTE ---

    private Integer getEmpresaId(HttpSession session) {
        Integer id = (Integer) session.getAttribute("idEmpresa");
        if (id == null) throw new RuntimeException("Sesión caducada.");
        return id;
    }

    private Integer getUsuarioId(HttpSession session) {
        Integer id = (Integer) session.getAttribute("idUsuario");
        if (id == null) throw new RuntimeException("Usuario no identificado.");
        return id;
    }

    private Integer getSucursalId(HttpSession session) {
        Integer id = (Integer) session.getAttribute("idSucursal");
        return (id != null) ? id : 1;
    }
}