package com.puntofacil.puntofacilbackend.service;

import com.opencsv.CSVReader;
import com.puntofacil.puntofacilbackend.entity.*;
import com.puntofacil.puntofacilbackend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // =========================================================================
    // 1. BÚSQUEDAS Y PAGINACIÓN
    // =========================================================================

    /**
     * Búsqueda paginada adaptada para soportar el filtro de Tipo (Producto/Servicio).
     * @param q Filtro de texto (nombre, barras, categoría)
     * @param idEmpresa ID de la empresa actual
     * @param tipo ID del tipo de producto (1: Producto, 2: Servicio, null: Todos)
     */
    public Page<Producto> buscarPaginado(String q, Integer idEmpresa, Integer tipo, Pageable pageable) {
        String query = (q == null) ? "" : q.trim();
        // delegamos al repositorio con el nuevo parámetro 'tipo'
        return productoRepository.buscarPaginado(query, idEmpresa, tipo, pageable);
    }

    public List<Producto> buscar(String q, Integer idEmpresa, boolean esPos) {
        String query = (q == null) ? "" : q.trim();
        if (esPos && query.isEmpty()) return List.of();
        return productoRepository.buscar(query, idEmpresa);
    }

    public Optional<Producto> findById(Integer id) {
        return productoRepository.findById(id);
    }

    // =========================================================================
    // 2. OPERACIONES DE PERSISTENCIA
    // =========================================================================

    @Transactional
    public Producto save(Producto producto) {
        // Normalización de strings
        if (producto.getNombre() != null) producto.setNombre(producto.getNombre().trim().toUpperCase());
        if (producto.getCodigoBarra() != null) producto.setCodigoBarra(producto.getCodigoBarra().trim());
        if (producto.getCodigoInterno() != null) producto.setCodigoInterno(producto.getCodigoInterno().trim().toUpperCase());

        // Valores por defecto
        if (producto.getIdTipoProducto() == null) producto.setIdTipoProducto(1);
        if (producto.getEsFavorito() == null) producto.setEsFavorito(0);
        if (producto.getFactorConversion() == null || producto.getFactorConversion().compareTo(BigDecimal.ZERO) <= 0) {
            producto.setFactorConversion(BigDecimal.ONE);
        }

        limpiarRelaciones(producto);

        if (producto.getIdProducto() != null) {
            return productoRepository.findById(producto.getIdProducto()).map(existente -> {
                producto.setActivo(existente.getActivo());
                producto.setIdEmpresa(existente.getIdEmpresa());

                // Solo los Productos (tipo 1) mantienen inventario físico
                if (producto.getIdTipoProducto() == 1) {
                    gestionarInventarioActualizacion(producto, existente);
                } else {
                    producto.setInventario(null); // Los servicios no tienen stock
                }
                return productoRepository.save(producto);
            }).orElseGet(() -> crearNuevoProducto(producto));
        } else {
            return crearNuevoProducto(producto);
        }
    }

    @Transactional
    public void anular(Integer id) {
        productoRepository.findById(id).ifPresent(p -> {
            p.setActivo(false);
            productoRepository.save(p);
        });
    }

    // =========================================================================
    // 3. GESTIÓN DE INVENTARIO INTERNA
    // =========================================================================

    private void gestionarInventarioActualizacion(Producto nuevo, Producto existente) {
        if (nuevo.getInventario() != null) {
            Inventario inv = nuevo.getInventario();
            inv.setProducto(nuevo);
            inv.setIdEmpresa(nuevo.getIdEmpresa());
            if (inv.getIdSucursal() == null) inv.setIdSucursal(1);

            // Mantenemos el stock actual para evitar que la edición lo resetee a 0
            inv.setStockActual(existente.getInventario() != null ?
                    existente.getInventario().getStockActual() : BigDecimal.ZERO);
        }
    }

    private Producto crearNuevoProducto(Producto p) {
        p.setActivo(true);
        if (p.getIdTipoProducto() == 1) {
            if (p.getInventario() == null) {
                Inventario inv = new Inventario();
                inv.setProducto(p);
                inv.setIdEmpresa(p.getIdEmpresa());
                inv.setIdSucursal(1);
                inv.setStockActual(BigDecimal.ZERO);
                inv.setStockMinimo(new BigDecimal("5.0"));
                inv.setStockMaximo(new BigDecimal("100.0"));
                p.setInventario(inv);
            } else {
                p.getInventario().setProducto(p);
                p.getInventario().setIdEmpresa(p.getIdEmpresa());
                p.getInventario().setStockActual(BigDecimal.ZERO);
                if (p.getInventario().getIdSucursal() == null) p.getInventario().setIdSucursal(1);
            }
        } else {
            p.setInventario(null);
        }
        return productoRepository.save(p);
    }

    // =========================================================================
    // 4. IMPORTACIÓN Y AUXILIARES
    // =========================================================================

    @Transactional
    public void importarDesdeCSV(InputStream inputStream, Integer idEmpresa) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String[] linea;
            while ((linea = reader.readNext()) != null) {
                try {
                    if (linea.length < 2 || linea[1].trim().isEmpty()) continue;

                    String codigoBarra = linea[0].trim();
                    if (!codigoBarra.isEmpty() && productoRepository.existsByCodigoBarraAndIdEmpresa(codigoBarra, idEmpresa)) continue;

                    Producto p = new Producto();
                    p.setCodigoBarra(codigoBarra);
                    p.setNombre(linea[1].trim().toUpperCase());
                    p.setIdEmpresa(idEmpresa);
                    p.setActivo(true);
                    p.setFactorConversion(BigDecimal.ONE);
                    p.setCostoUltimo(linea.length > 2 ? parseDoubleSafely(linea[2]) : 0.0);
                    p.setPrecio(linea.length > 3 ? parseDoubleSafely(linea[3]) : 0.0);

                    // Lógica de inventario para importación
                    if (linea.length >= 7 && "1".equals(linea[6])) {
                        Inventario inv = new Inventario();
                        inv.setProducto(p);
                        inv.setIdEmpresa(idEmpresa);
                        inv.setIdSucursal(1);
                        inv.setStockActual(BigDecimal.ZERO);
                        inv.setStockMinimo(linea.length > 4 ? parseBigDecimalSafely(linea[4], 5.0) : new BigDecimal("5.0"));
                        p.setInventario(inv);
                    }
                    productoRepository.save(p);
                } catch (Exception e) {
                    // Ignorar errores en líneas individuales para continuar con el resto
                }
            }
        }
    }

    private void limpiarRelaciones(Producto p) {
        if (p.getUnidadVenta() != null && p.getUnidadVenta().getIdUnidad() == null) p.setUnidadVenta(null);
        if (p.getUnidadCompra() != null && p.getUnidadCompra().getIdUnidad() == null) p.setUnidadCompra(null);
        if (p.getMarca() != null && p.getMarca().getIdMarca() == null) p.setMarca(null);
        if (p.getFamilia() != null && p.getFamilia().getIdFamilia() == null) p.setFamilia(null);
    }

    private Double parseDoubleSafely(String val) {
        try { return Double.parseDouble(val.trim()); } catch (Exception e) { return 0.0; }
    }

    private BigDecimal parseBigDecimalSafely(String val, double def) {
        try { return new BigDecimal(val.trim()); } catch (Exception e) { return BigDecimal.valueOf(def); }
    }
}