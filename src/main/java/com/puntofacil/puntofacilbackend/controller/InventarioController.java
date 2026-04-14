package com.puntofacil.puntofacilbackend.controller;

import com.puntofacil.puntofacilbackend.entity.Inventario;
import com.puntofacil.puntofacilbackend.entity.InventarioKardex;
import com.puntofacil.puntofacilbackend.entity.Producto;
import com.puntofacil.puntofacilbackend.repository.InventarioKardexRepository;
import com.puntofacil.puntofacilbackend.repository.ProductoRepository;
import com.puntofacil.puntofacilbackend.service.InventarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioKardexRepository kardexRepository;

    /**
     * Muestra la pantalla de mantenimiento de stock y movimientos recientes.
     */
    @GetMapping("/mantenimiento")
    public String mantenimiento(Model model, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);

            // Listar solo productos de la empresa actual
            model.addAttribute("productos", productoRepository.findByActivoTrueAndIdEmpresa(idEmpresa));

            // Movimientos recientes filtrados por empresa
            List<InventarioKardex> recientes = kardexRepository.findTop10Movimientos(idEmpresa);
            model.addAttribute("recientes", recientes);

            return "inventario/mantenimiento";
        } catch (RuntimeException e) {
            return "redirect:/login?error=session_expired";
        }
    }

    /**
     * PROCESAR AJUSTE MANUAL (Entradas/Salidas manuales)
     */
    @PostMapping("/ajuste/guardar")
    public String guardarAjusteManual(@RequestParam("idProducto") Integer idProducto,
                                      @RequestParam("cantidad") BigDecimal cantidad,
                                      @RequestParam(value = "costoUnitario", required = false) BigDecimal costoUnitario,
                                      @RequestParam("tipoAjuste") String tipoAjuste,
                                      @RequestParam("motivo") String motivo,
                                      HttpSession session,
                                      RedirectAttributes flash) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            Integer idUsuario = getUsuarioId(session);
            Integer idSucursal = getSucursalId(session);

            Producto producto = productoRepository.findById(idProducto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado."));

            if (!producto.getIdEmpresa().equals(idEmpresa)) {
                throw new RuntimeException("No tiene permisos sobre este producto.");
            }

            BigDecimal costoFinal = (costoUnitario != null) ? costoUnitario : BigDecimal.ZERO;

            inventarioService.registrarMovimientoInventario(
                    idProducto, idSucursal, idEmpresa, cantidad, costoFinal,
                    tipoAjuste, motivo, idUsuario, "MANUAL"
            );

            flash.addFlashAttribute("success", "Movimiento de inventario registrado con éxito.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al procesar ajuste: " + e.getMessage());
        }
        return "redirect:/inventario/mantenimiento";
    }

    /**
     * PROCESAR CARGA MASIVA CSV
     */
    @PostMapping("/carga-masiva/procesar")
    public String procesarCargaMasiva(@RequestParam("archivo") MultipartFile archivo,
                                      HttpSession session,
                                      RedirectAttributes flash) {
        if (archivo.isEmpty()) {
            flash.addFlashAttribute("error", "Por favor, seleccione un archivo CSV válido.");
            return "redirect:/inventario/mantenimiento";
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            Integer idEmpresa = getEmpresaId(session);
            Integer idUsuario = getUsuarioId(session);
            Integer idSucursal = getSucursalId(session);

            String linea;
            int contExitos = 0;
            int contErrores = 0;

            br.readLine(); // Saltar cabecera

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String separator = linea.contains(";") ? ";" : ",";
                String[] datos = linea.split(separator);

                try {
                    String codigoBarra = datos[0].trim();
                    BigDecimal cantidad = new BigDecimal(datos[10].trim());

                    BigDecimal costo = (datos.length > 11 && !datos[11].isEmpty())
                            ? new BigDecimal(datos[11].trim())
                            : BigDecimal.ZERO;

                    Optional<Producto> productoOpt = productoRepository.findByCodigoBarraAndIdEmpresaAndActivoTrue(codigoBarra, idEmpresa);

                    if (productoOpt.isPresent()) {
                        inventarioService.registrarMovimientoInventario(
                                productoOpt.get().getIdProducto(), idSucursal, idEmpresa,
                                cantidad, costo, "INGRESO", "Carga masiva CSV", idUsuario, "CSV"
                        );
                        contExitos++;
                    } else {
                        contErrores++;
                    }
                } catch (Exception e) {
                    contErrores++;
                }
            }
            flash.addFlashAttribute("success", "Proceso completado. Éxitos: " + contExitos + " | Errores: " + contErrores);

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al leer el archivo: " + e.getMessage());
        }

        return "redirect:/inventario/mantenimiento";
    }

    /**
     * API para notificaciones de stock bajo
     */
    @GetMapping("/api/faltantes")
    @ResponseBody
    public List<Inventario> getFaltantes(HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            return inventarioService.obtenerStockBajo(idEmpresa);
        } catch (Exception e) {
            return List.of(); // Devolver lista vacía si la sesión falla
        }
    }

    // --- MÉTODOS DE SOPORTE PARA SESIÓN (ESTANDARIZADOS) ---

    private Integer getEmpresaId(HttpSession session) {
        // Ahora solo busca "idEmpresa"
        Integer id = (Integer) session.getAttribute("idEmpresa");
        if (id == null) {
            throw new RuntimeException("Sesión caducada. Por favor, inicie sesión de nuevo.");
        }
        return id;
    }

    private Integer getUsuarioId(HttpSession session) {
        // Ahora solo busca "idUsuario"
        Integer id = (Integer) session.getAttribute("idUsuario");
        if (id == null) {
            throw new RuntimeException("Usuario no identificado.");
        }
        return id;
    }

    private Integer getSucursalId(HttpSession session) {
        // Ahora solo busca "idSucursal"
        Integer id = (Integer) session.getAttribute("idSucursal");
        return (id != null) ? id : 1;
    }
}