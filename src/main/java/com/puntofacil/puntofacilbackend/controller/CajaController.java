package com.puntofacil.puntofacilbackend.controller;

import com.puntofacil.puntofacilbackend.entity.CajaSesion;
import com.puntofacil.puntofacilbackend.entity.CajaMovimiento;
import com.puntofacil.puntofacilbackend.repository.CajaSesionRepository;
import com.puntofacil.puntofacilbackend.repository.CajaMovimientoRepository;
import com.puntofacil.puntofacilbackend.service.CajaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/caja")
public class CajaController {

    @Autowired
    private CajaSesionRepository sesionRepo;

    @Autowired
    private CajaMovimientoRepository movimientoRepo;

    @Autowired
    private CajaService cajaService;

    // =========================
    // VISTA: GESTIÓN
    // =========================
    @GetMapping("/gestion")
    public String verGestion(
            @RequestParam(name = "fInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fInicio,
            @RequestParam(name = "fFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fFin,
            @RequestParam(name = "sucursal", required = false) Integer sucursal,
            Model model, HttpSession session) {

        try {
            Integer idEmpresa = getEmpresaId(session);

            LocalDateTime inicio = (fInicio != null)
                    ? fInicio.atStartOfDay()
                    : LocalDate.now().minusDays(30).atStartOfDay();

            LocalDateTime fin = (fFin != null)
                    ? fFin.atTime(23, 59, 59)
                    : LocalDateTime.now();

            List<CajaSesion> sesiones = sesionRepo.buscarSesionesHistoricas(idEmpresa, sucursal, inicio, fin);

            model.addAttribute("sesiones", sesiones);
            model.addAttribute("fInicio", fInicio);
            model.addAttribute("fFin", fFin);
            model.addAttribute("sucursalSeleccionada", sucursal);

            return "caja/gestion";

        } catch (RuntimeException e) {
            return "redirect:/login?error=session_expired";
        }
    }

    // =========================
    // VISTA: DETALLE SESIÓN
    // =========================
    @GetMapping("/detalle/{id}")
    public String verDetalleSesion(@PathVariable("id") Integer id, Model model, HttpSession session) {

        try {
            Integer idEmpresa = getEmpresaId(session);

            CajaSesion sesion = sesionRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

            if (!sesion.getIdEmpresa().equals(idEmpresa)) {
                return "redirect:/caja/gestion?error=access_denied";
            }

            // 1. Obtener todos los movimientos
            List<CajaMovimiento> movimientos = movimientoRepo
                    .findByIdSesionOrderByFechaMovimientoDesc(id);

            // 2. Resumen de Ventas por Método (Para tarjetas informativas)
            List<Map<String, Object>> resumenPagos = movimientoRepo
                    .obtenerResumenVentasPorMetodo(id);

            // 3. Calcular Total Ventas
            BigDecimal totalVentas = resumenPagos.stream()
                    .map(m -> convertirABigDecimal(m.get("total")))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 4. Agrupar movimientos de INGRESO por método (Para el acordeón detallado)
            Map<String, List<CajaMovimiento>> detallePorMetodo = movimientos.stream()
                    .filter(m -> "INGRESO".equalsIgnoreCase(m.getTipoMovimiento()))
                    .collect(Collectors.groupingBy(m -> {
                        String metodo = m.getMetodoPago();
                        return (metodo != null && !metodo.isEmpty()) ? metodo : "OTROS";
                    }));

            // 5. NUEVO: Calcular los totales por grupo en Java para evitar error SpEL en Thymeleaf
            Map<String, BigDecimal> totalesPorMetodo = detallePorMetodo.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().stream()
                                    .map(CajaMovimiento::getMonto)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    ));

            // 6. NUEVO: Calcular diferencia de cierre
            BigDecimal real = sesion.getMontoCierreReal() != null ? sesion.getMontoCierreReal() : BigDecimal.ZERO;
            BigDecimal esperado = sesion.getMontoCierreSistema() != null ? sesion.getMontoCierreSistema() : BigDecimal.ZERO;
            BigDecimal diferencia = real.subtract(esperado);

            // Inyección al modelo
            model.addAttribute("sesion", sesion);
            model.addAttribute("movimientos", movimientos);
            model.addAttribute("resumenPagos", resumenPagos);
            model.addAttribute("totalVentas", totalVentas);
            model.addAttribute("detallePorMetodo", detallePorMetodo);
            model.addAttribute("totalesPorMetodo", totalesPorMetodo); // Mapa de totales listo
            model.addAttribute("diferenciaCierre", diferencia);      // Diferencia lista

            return "caja/detalle_sesion";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/caja/gestion";
        }
    }

    // =========================
    // API: ESTADO
    // =========================
    @GetMapping("/estado")
    @ResponseBody
    public ResponseEntity<?> verificarEstado(HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            Map<String, Object> res = new HashMap<>();

            cajaService.obtenerSesionActiva(idEmpresa).ifPresentOrElse(
                    s -> {
                        res.put("activa", true);
                        res.put("sesion", s);
                    },
                    () -> res.put("activa", false)
            );

            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================
    // API: ABRIR CAJA
    // =========================
    @PostMapping("/abrir")
    @ResponseBody
    public ResponseEntity<?> abrirCaja(@RequestBody Map<String, Object> datos, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            Integer idUsuario = getUsuarioId(session);

            if (cajaService.tieneSesionActiva(idEmpresa)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ya existe una sesión abierta."));
            }

            BigDecimal montoInicial = convertirABigDecimal(datos.get("monto_apertura"));

            CajaSesion nueva = new CajaSesion();
            nueva.setIdEmpresa(idEmpresa);
            nueva.setIdSucursal(getSucursalId(session));
            nueva.setIdUsuario(idUsuario);
            nueva.setFechaApertura(LocalDateTime.now());
            nueva.setMontoApertura(montoInicial);
            nueva.setEstado("ABIERTA");
            nueva.setIdCaja(1);

            return ResponseEntity.ok(sesionRepo.save(nueva));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================
    // API: CERRAR CAJA
    // =========================
    @PostMapping("/cerrar")
    @ResponseBody
    public ResponseEntity<?> cerrarCaja(@RequestBody Map<String, Object> datos, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            BigDecimal montoReal = convertirABigDecimal(datos.get("monto_real"));

            CajaSesion sesion = cajaService.obtenerSesionActiva(idEmpresa)
                    .orElseThrow(() -> new RuntimeException("No hay sesión abierta."));

            BigDecimal ingresos = Optional.ofNullable(
                    movimientoRepo.sumMontoByIdSesionAndTipo(sesion.getIdSesion(), "INGRESO")
            ).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);

            BigDecimal egresos = Optional.ofNullable(
                    movimientoRepo.sumMontoByIdSesionAndTipo(sesion.getIdSesion(), "EGRESO")
            ).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);

            BigDecimal apertura = Optional.ofNullable(sesion.getMontoApertura())
                    .orElse(BigDecimal.ZERO);

            BigDecimal esperado = apertura.add(ingresos).subtract(egresos);

            sesion.setFechaCierre(LocalDateTime.now());
            sesion.setMontoCierreSistema(esperado);
            sesion.setMontoCierreReal(montoReal);
            sesion.setEstado("CERRADA");

            sesionRepo.save(sesion);

            return ResponseEntity.ok(Map.of(
                    "esperado", esperado,
                    "real", montoReal,
                    "diferencia", montoReal.subtract(esperado)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================
    // HELPERS
    // =========================

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

    private BigDecimal convertirABigDecimal(Object obj) {
        try {
            return (obj == null) ? BigDecimal.ZERO : new BigDecimal(obj.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}