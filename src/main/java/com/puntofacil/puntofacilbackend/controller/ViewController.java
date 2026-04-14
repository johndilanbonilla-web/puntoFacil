package com.puntofacil.puntofacilbackend.controller;

import com.puntofacil.puntofacilbackend.entity.Venta;
import com.puntofacil.puntofacilbackend.repository.CajaSesionRepository;
import com.puntofacil.puntofacilbackend.repository.VentaRepository;
import com.puntofacil.puntofacilbackend.repository.ProductoRepository;
import com.puntofacil.puntofacilbackend.repository.GastoRepository; // Importación necesaria
import com.puntofacil.puntofacilbackend.service.BancoService;
import com.puntofacil.puntofacilbackend.service.FormaPagoService;
import com.puntofacil.puntofacilbackend.service.VentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ViewController {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CajaSesionRepository cajaSesionRepository;

    @Autowired
    private GastoRepository gastoRepository; // Inyectado para el cálculo de gastos

    @Autowired
    private FormaPagoService formaPagoService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private BancoService bancoService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * DASHBOARD PRINCIPAL - Adaptado para mostrar Gastos y Estado de Caja real
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            Integer idUsuario = getUsuarioId(session);

            // Definición de rangos de tiempo para "Hoy"
            LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
            LocalDateTime finHoy = LocalDate.now().atTime(23, 59, 59);

            // 1. Datos de Ventas
            BigDecimal totalVentasHoy = ventaRepository.sumTotalVentasHoyByEmpresa(inicioHoy, idEmpresa);
            Long conteoVentasHoy = ventaRepository.countVentasHoyByEmpresa(inicioHoy, idEmpresa);

            // 2. Datos de Gastos
            BigDecimal totalGastosHoy = gastoRepository.sumTotalGastosEntreFechasByEmpresa(inicioHoy, finHoy, idEmpresa);

            // 3. Stock e Inventario
            Long stockBajo = productoRepository.countStockBajo(idEmpresa);

            // 4. Lógica de Caja (Sincronizada con el Dashboard)
            boolean estaAbierta = cajaSesionRepository.findByEstadoAndIdUsuarioAndIdEmpresa("ABIERTA", idUsuario, idEmpresa).isPresent();

            // 5. Gráfico de Rendimiento Semanal
            List<BigDecimal> ventasSemana = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate dia = LocalDate.now().minusDays(i);
                BigDecimal totalDia = ventaRepository.sumTotalVentasEntreFechasByEmpresa(
                        dia.atStartOfDay(),
                        dia.atTime(23, 59, 59),
                        idEmpresa
                );
                ventasSemana.add(totalDia != null ? totalDia : BigDecimal.ZERO);
            }

            // --- Atributos enviados al Model (Thymeleaf) ---
            model.addAttribute("totalVentasHoy", totalVentasHoy != null ? totalVentasHoy : BigDecimal.ZERO);
            model.addAttribute("conteoVentasHoy", conteoVentasHoy != null ? conteoVentasHoy : 0L);
            model.addAttribute("totalGastosHoy", totalGastosHoy != null ? totalGastosHoy : BigDecimal.ZERO);
            model.addAttribute("productosBajos", stockBajo != null ? stockBajo : 0L);
            model.addAttribute("ventasSemana", ventasSemana);

            // Variables críticas para el estado de la caja
            model.addAttribute("cajaAbierta", estaAbierta);
            model.addAttribute("estadoCaja", estaAbierta ? "ABIERTA" : "CERRADA");

            return "dashboard";
        } catch (RuntimeException e) {
            return "redirect:/login?error=session_expired";
        }
    }

    @GetMapping("/pos")
    public String nuevaVenta(Model model, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            Integer idUsuario = getUsuarioId(session);

            boolean estaAbierta = cajaSesionRepository.findByEstadoAndIdUsuarioAndIdEmpresa("ABIERTA", idUsuario, idEmpresa).isPresent();

            model.addAttribute("cajaAbierta", estaAbierta);
            model.addAttribute("formasPago", formaPagoService.listarActivas());
            model.addAttribute("bancos", bancoService.listarBancosPorEmpresa(idEmpresa));

            return "ventas/pos";
        } catch (RuntimeException e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/ventas")
    public String listarVentas(Model model, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            model.addAttribute("ventas", ventaRepository.findAllByIdEmpresaOrderByFechaVentaDesc(idEmpresa));
            return "ventas/lista";
        } catch (RuntimeException e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/productos/api/pos/botones-rapidos")
    @ResponseBody
    public Map<String, Object> getBotonesRapidos(HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            Map<String, Object> response = new HashMap<>();
            response.put("favoritos", productoRepository.findByEsFavoritoTrueAndIdEmpresaAndActivoTrue(idEmpresa));
            response.put("topVendidos", productoRepository.findTopVendidos(idEmpresa));
            return response;
        } catch (Exception e) {
            return Map.of("error", "Sesión inválida");
        }
    }

    @GetMapping("/ventas/ticket/{id}")
    public String verTicket(@PathVariable("id") Integer id, Model model, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            Venta venta = ventaService.obtenerVentaPorIdYEmpresa(id, idEmpresa);
            model.addAttribute("venta", venta);
            model.addAttribute("detalles", venta.getDetalles());
            return "ventas/ticket";
        } catch (Exception e) {
            return "redirect:/dashboard";
        }
    }

    private Integer getEmpresaId(HttpSession session) {
        Integer id = (Integer) session.getAttribute("idEmpresa");
        if (id == null) throw new RuntimeException("Sesión caducada");
        return id;
    }

    private Integer getUsuarioId(HttpSession session) {
        Integer id = (Integer) session.getAttribute("idUsuario");
        if (id == null) throw new RuntimeException("Usuario no identificado");
        return id;
    }
}