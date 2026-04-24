package com.puntofacil.puntofacilbackend.controller;

import com.puntofacil.puntofacilbackend.entity.CajaSesion;
import com.puntofacil.puntofacilbackend.entity.CajaMovimiento;
import com.puntofacil.puntofacilbackend.repository.CajaSesionRepository;
import com.puntofacil.puntofacilbackend.repository.CajaMovimientoRepository;
import com.puntofacil.puntofacilbackend.service.CajaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/caja")
public class CajaController {

    @Autowired
    private CajaSesionRepository sesionRepo;

    @Autowired
    private CajaMovimientoRepository movimientoRepo;

    @Autowired
    private CajaService cajaService;

    // ==========================================
    // VISTA: GESTIÓN (HISTORIAL)
    // ==========================================
    @GetMapping("/gestion")
    public String verGestion(
            @RequestParam(name = "fInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fInicio,
            @RequestParam(name = "fFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fFin,
            @RequestParam(name = "sucursal", required = false) Integer sucursal,
            Model model, HttpSession session) {

        try {
            Integer idEmpresa = getEmpresaId(session);
            LocalDate inicio = (fInicio != null) ? fInicio : LocalDate.now().minusDays(30);
            LocalDate fin = (fFin != null) ? fFin : LocalDate.now();

            // El Service ahora se encarga de devolver la lista y llenar los Transientes
            List<CajaSesion> sesiones = cajaService.obtenerHistorialGestion(idEmpresa, sucursal, inicio, fin);

            model.addAttribute("sesiones", sesiones);
            model.addAttribute("fInicio", inicio);
            model.addAttribute("fFin", fin);

            return "caja/gestion";
        } catch (Exception e) {
            return "redirect:/login?error=session_expired";
        }
    }

    // ==========================================
    // VISTA: DETALLE DE AUDITORÍA
    // ==========================================
    @GetMapping("/detalle/{id}")
    public String verDetalleAuditoria(@PathVariable("id") Integer id, Model model, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            CajaSesion sesion = sesionRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

            if (!sesion.getIdEmpresa().equals(idEmpresa)) return "redirect:/caja/gestion";

            // 1. Resumen de métodos de pago
            List<Map<String, Object>> resumenPagos = movimientoRepo.obtenerResumenVentasPorMetodo(id);

            // 2. Usar el Service para calcular el arqueo oficial (Centralización de lógica)
            BigDecimal montoEsperado = cajaService.calcularEfectivoEsperado(sesion);

            // Ya que CierreReal puede ser nulo si la caja sigue abierta
            BigDecimal realCierre = sesion.getMontoCierreReal() != null ? sesion.getMontoCierreReal() : BigDecimal.ZERO;
            BigDecimal diferencia = realCierre.subtract(montoEsperado);

            // 3. Separar movimientos para la vista
            List<CajaMovimiento> movimientos = movimientoRepo.findByIdSesion(id);
            List<CajaMovimiento> ventasSesion = movimientos.stream()
                    .filter(m -> "INGRESO".equals(m.getTipoMovimiento())).toList();
            List<CajaMovimiento> gastosSesion = movimientos.stream()
                    .filter(m -> "EGRESO".equals(m.getTipoMovimiento())).toList();

            // 4. Inyectar al HTML (Asegurar compatibilidad con el Dark Mode)
            model.addAttribute("sesion", sesion);
            model.addAttribute("totalVentas", sesionRepo.sumTotalVentasBySesion(id)); // BigDecimal
            model.addAttribute("totalGastos", sesionRepo.sumTotalGastosBySesion(id).add(sesionRepo.sumTotalComprasBySesion(id)));
            model.addAttribute("resumenPagos", resumenPagos);
            model.addAttribute("ventasSesion", ventasSesion);
            model.addAttribute("gastosSesion", gastosSesion);
            model.addAttribute("montoEsperado", montoEsperado);
            model.addAttribute("diferenciaCierre", diferencia);

            return "caja/detalle_sesion";

        } catch (Exception e) {
            return "redirect:/caja/gestion?error=not_found";
        }
    }

    // ==========================================
    // API ENDPOINTS (Axios / Fetch desde el Frontend)
    // ==========================================
    @GetMapping("/estado")
    @ResponseBody
    public ResponseEntity<?> obtenerEstadoCaja(HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            Integer idUsuario = getUsuarioId(session);
            Optional<CajaSesion> sesionOpt = cajaService.obtenerSesionActiva(idEmpresa, idUsuario);

            if (sesionOpt.isPresent()) {
                CajaSesion sesion = sesionOpt.get();
                BigDecimal ventas = sesionRepo.sumTotalVentasBySesion(sesion.getIdSesion());
                BigDecimal gastos = sesionRepo.sumTotalGastosBySesion(sesion.getIdSesion());

                // EL DATO CLAVE:
                BigDecimal efectivoEsperado = cajaService.calcularEfectivoEsperado(sesion);

                return ResponseEntity.ok(Map.of(
                        "activa", true,
                        "sesion", Map.of(
                                "idSesion", sesion.getIdSesion(),
                                "montoApertura", sesion.getMontoApertura(),
                                "totalVentas", ventas,
                                "totalGastos", gastos,
                                "efectivoEsperado", efectivoEsperado // <-- Enviamos esto al JS
                        )
                ));
            }
            return ResponseEntity.ok(Map.of("activa", false));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/abrir")
    @ResponseBody
    public ResponseEntity<?> abrirCaja(@RequestBody Map<String, Object> datos, HttpSession session) {
        try {
            CajaSesion nueva = new CajaSesion();
            nueva.setIdEmpresa(getEmpresaId(session));
            nueva.setIdSucursal(getSucursalId(session));
            nueva.setIdUsuario(getUsuarioId(session));
            nueva.setIdCaja(1); // O tomar del DTO si manejas múltiples cajas físicas
            nueva.setMontoApertura(parseToBigDecimal(datos.get("monto_apertura")));

            // DELEGACIÓN: El Service se encarga de guardar y crear el movimiento de auditoría
            CajaSesion sesionAbierta = cajaService.abrirCaja(nueva);

            return ResponseEntity.ok(sesionAbierta);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error interno al abrir la caja."));
        }
    }

    @PostMapping("/cerrar")
    @ResponseBody
    public ResponseEntity<?> cerrarCaja(@RequestBody Map<String, Object> datos, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            Integer idUsuario = getUsuarioId(session);
            BigDecimal montoReal = parseToBigDecimal(datos.get("monto_real"));

            CajaSesion sesionActiva = cajaService.obtenerSesionActiva(idEmpresa, idUsuario)
                    .orElseThrow(() -> new RuntimeException("No hay sesión abierta."));

            // DELEGACIÓN: El Service se encarga de calcular el cuadre y cerrar la caja de forma segura
            cajaService.cerrarCaja(sesionActiva.getIdSesion(), montoReal);

            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ==========================================
    // HELPERS (Refactorizados a BigDecimal)
    // ==========================================
    private Integer getEmpresaId(HttpSession session) {
        Integer id = (Integer) session.getAttribute("idEmpresa");
        if (id == null) throw new RuntimeException("Sesión caducada.");
        return id;
    }

    private Integer getUsuarioId(HttpSession session) {
        return (Integer) session.getAttribute("idUsuario");
    }

    private Integer getSucursalId(HttpSession session) {
        Integer id = (Integer) session.getAttribute("idSucursal");
        return (id != null) ? id : 1;
    }

    private BigDecimal parseToBigDecimal(Object obj) {
        try {
            return (obj == null || obj.toString().isBlank()) ? BigDecimal.ZERO : new BigDecimal(obj.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}