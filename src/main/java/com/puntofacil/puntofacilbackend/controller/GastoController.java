package com.puntofacil.puntofacilbackend.controller;

import com.puntofacil.puntofacilbackend.dto.GastoDTO;
import com.puntofacil.puntofacilbackend.entity.Gasto;
import com.puntofacil.puntofacilbackend.service.GastoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/gastos")
public class GastoController {

    @Autowired private GastoService gastoService;
    @Autowired private JdbcTemplate jdbcTemplate;

    @GetMapping("")
    public String index() {
        return "redirect:/gastos/lista";
    }

    /**
     * Lista los gastos con normalización de estados y fechas.
     */
    @GetMapping("/lista")
    public String listarGastos(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            HttpSession session,
            Model model) {
        try {
            Integer idEmpresa = getEmpresaId(session);

            if (desde == null || desde.isEmpty()) desde = LocalDate.now().minusWeeks(1).toString();
            if (hasta == null || hasta.isEmpty()) hasta = LocalDate.now().toString();

            List<Map<String, Object>> gastos = gastoService.obtenerGastosPorEmpresaYRango(idEmpresa, desde, hasta);

            // Normalización para compatibilidad con HTML existente y Thymeleaf
            for (Map<String, Object> g : gastos) {
                // Fechas
                convertirTimestampALocalDateTime(g, "fecha");
                convertirTimestampALocalDateTime(g, "fecha_gasto");
                convertirTimestampALocalDateTime(g, "fecha_anulacion");

                // Mapeo de estado String a Booleano para el CSS del HTML
                if (g.containsKey("estado")) {
                    g.put("activo", "ACTIVO".equals(g.get("estado")));
                }
            }

            model.addAttribute("gastos", gastos);
            model.addAttribute("fechaDesde", desde);
            model.addAttribute("fechaHasta", hasta);

            return "gasto/listagasto";
        } catch (RuntimeException e) {
            return "redirect:/login?error=session_expired";
        }
    }

    /**
     * Vista del ticket detallada con soporte para auditoría.
     */
    @GetMapping("/imprimir/{id}")
    public String imprimirTicket(@PathVariable Integer id, HttpSession session, Model model) {
        try {
            Map<String, Object> gastoInfo = gastoService.obtenerGastoPorId(id);

            if (gastoInfo == null || gastoInfo.isEmpty()) {
                return "redirect:/gastos/lista?error=not_found";
            }

            // Normalizamos todas las fechas posibles en el mapa
            convertirTimestampALocalDateTime(gastoInfo, "fecha");
            convertirTimestampALocalDateTime(gastoInfo, "fecha_gasto");
            convertirTimestampALocalDateTime(gastoInfo, "fecha_anulacion");

            model.addAttribute("gasto", gastoInfo);
            return "gasto/comprobante";
        } catch (Exception e) {
            return "redirect:/gastos/lista?error=print_failed";
        }
    }

    @GetMapping("/nuevo")
    public String vistaNuevoGasto(HttpSession session, Model model) {
        try {
            Integer idEmpresa = getEmpresaId(session);

            // Filtramos solo productos de la familia GASTOS
            String sqlProductos =
                    "SELECT p.id_producto, p.nombre FROM producto p " +
                            "INNER JOIN familia f ON p.id_familia = f.id_familia " +
                            "WHERE UPPER(f.nombre) = 'GASTOS' " +
                            "AND p.id_empresa = ? AND p.activo = true";

            model.addAttribute("productosGasto", jdbcTemplate.queryForList(sqlProductos, idEmpresa));
            model.addAttribute("formasPago", jdbcTemplate.queryForList("SELECT id_forma_pago, nombre FROM forma_pago WHERE activo = true"));
            model.addAttribute("bancos", jdbcTemplate.queryForList("SELECT id_banco, nombre_banco AS nombre FROM banco WHERE id_empresa = ? AND activo = true", idEmpresa));

            return "gasto/gasto";
        } catch (Exception e) {
            return "redirect:/gastos/lista?error=db_error";
        }
    }

    @PostMapping("/guardar")
    @ResponseBody
    public ResponseEntity<?> guardarGasto(@RequestBody GastoDTO dto, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            Integer idUsuario = getUsuarioId(session);
            Gasto guardado = gastoService.registrarGasto(dto, idUsuario, idEmpresa);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "mensaje", "Gasto registrado correctamente.",
                    "idGasto", guardado.getIdGasto()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "mensaje", e.getMessage()));
        }
    }

    @PostMapping("/anular/{id}")
    @ResponseBody
    public ResponseEntity<?> anularGasto(@PathVariable Integer id, HttpSession session) {
        try {
            Integer idUsuario = getUsuarioId(session);
            gastoService.anularGasto(id, idUsuario);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "mensaje", "Gasto anulado correctamente."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "mensaje", e.getMessage()));
        }
    }

    // --- UTILIDADES ---

    private void convertirTimestampALocalDateTime(Map<String, Object> map, String key) {
        if (map != null && map.containsKey(key) && map.get(key) != null) {
            Object valor = map.get(key);
            if (valor instanceof java.sql.Timestamp ts) {
                map.put(key, ts.toLocalDateTime());
            } else if (valor instanceof java.util.Date d) {
                map.put(key, new java.sql.Timestamp(d.getTime()).toLocalDateTime());
            } else if (valor instanceof LocalDateTime) {
                // Ya es LocalDateTime, no hacemos nada
            }
        }
    }

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
}