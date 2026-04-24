/* =========================================================================
   CONTROLADOR DE PRODUCTOS ADAPTADO (FILTRO TIPO + PAGINACIÓN)
   ========================================================================= */

package com.puntofacil.puntofacilbackend.controller;

import com.puntofacil.puntofacilbackend.entity.Producto;
import com.puntofacil.puntofacilbackend.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private FamiliaService familiaService;

    // --- VISTA PRINCIPAL CON FILTRO DE TIPO ---
    @GetMapping("/mantenimiento")
    public String vistaMantenimiento(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Integer tipo, // Nuevo parámetro para filtrar Producto/Servicio
            Model model, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            Pageable pageable = PageRequest.of(page, 10);

            // Se asume que el Service ahora acepta el parámetro 'tipo'
            Page<Producto> pagina = productoService.buscarPaginado(q, idEmpresa, tipo, pageable);

            model.addAttribute("productos", pagina.getContent());
            model.addAttribute("paginaActual", page);
            model.addAttribute("totalPaginas", pagina.getTotalPages());
            model.addAttribute("busqueda", q);
            model.addAttribute("tipoFiltro", tipo); // Para mantener el valor en el <select>

            model.addAttribute("familias", familiaService.findAllByEmpresa(idEmpresa));
            model.addAttribute("version", System.currentTimeMillis());

            return "admin/productos_mantenimiento";
        } catch (RuntimeException e) {
            return "redirect:/login?error=session_expired";
        }
    }

    // --- BUSCADOR AJAX PARA FRAGMENTOS (ACTUALIZADO) ---
    @GetMapping("/mantenimiento/fragmento")
    public String buscarFragmento(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Integer tipo, // Parámetro enviado por el Fetch de JS
            HttpSession session, Model model) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            Pageable pageable = PageRequest.of(page, 10);

            // Buscamos con los tres criterios: query, empresa y tipo
            Page<Producto> pagina = productoService.buscarPaginado(q, idEmpresa, tipo, pageable);

            model.addAttribute("productos", pagina.getContent());
            model.addAttribute("paginaActual", page);
            model.addAttribute("totalPaginas", pagina.getTotalPages());
            model.addAttribute("busqueda", q);

            // IMPORTANTE: Devuelve 'lista_completa' si usaste el wrapper dinámico en el HTML
            return "admin/productos_mantenimiento :: lista_completa";
        } catch (Exception e) {
            return "admin/productos_mantenimiento :: lista_completa";
        }
    }

    // ==========================================
    // NUEVO PRODUCTO (El método que faltaba)
    // ==========================================
    @GetMapping("/nuevo")
    public String nuevoProducto(Model model, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);

            // 1. Instanciamos un Producto vacío para que Thymeleaf no explote al buscar th:field
            Producto nuevoProducto = new Producto();

            // 2. Inyectamos al modelo el producto y las listas necesarias para el formulario
            model.addAttribute("producto", nuevoProducto);
            model.addAttribute("familias", familiaService.findAllByEmpresa(idEmpresa));

            // 3. Retornamos exactamente la misma vista que usa el método editar
            return "admin/productos_form";

        } catch (RuntimeException e) {
            return "redirect:/login?error=session_expired";
        }
    }

    // --- GUARDAR ---
    @PostMapping("/guardar")
    @ResponseBody
    public ResponseEntity<?> guardar(@RequestBody Producto producto, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            producto.setIdEmpresa(idEmpresa);

            if (producto.getInventario() != null) {
                producto.getInventario().setIdEmpresa(idEmpresa);
                if (producto.getInventario().getIdSucursal() == null) {
                    producto.getInventario().setIdSucursal(getSucursalId(session));
                }
            }

            productoService.save(producto);
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // --- MÉTODOS DE APOYO ---

    @GetMapping("/editar/{id}")
    public String editarProducto(@PathVariable Integer id, Model model, HttpSession session) {
        try {
            Integer idEmpresa = getEmpresaId(session);
            Producto producto = productoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            model.addAttribute("producto", producto);
            model.addAttribute("familias", familiaService.findAllByEmpresa(idEmpresa));
            return "admin/productos_form";
        } catch (Exception e) {
            return "redirect:/productos/mantenimiento?error=not_found";
        }
    }

    @PostMapping("/anular/{id}")
    @ResponseBody
    public ResponseEntity<String> anular(@PathVariable Integer id) {
        try {
            productoService.anular(id);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al anular");
        }
    }

    private Integer getEmpresaId(HttpSession session) {
        Integer id = (Integer) session.getAttribute("idEmpresa");
        if (id == null) throw new RuntimeException("Sesión caducada.");
        return id;
    }

    private Integer getSucursalId(HttpSession session) {
        Integer id = (Integer) session.getAttribute("idSucursal");
        return (id != null) ? id : 1;
    }
}