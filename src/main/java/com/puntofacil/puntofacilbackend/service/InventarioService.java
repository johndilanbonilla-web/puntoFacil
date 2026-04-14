package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.entity.*;
import com.puntofacil.puntofacilbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private InventarioKardexRepository kardexRepository;

    @Autowired
    private InventarioAjusteRepository ajusteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Procesa un ajuste de inventario (Manual o desde CSV) incluyendo valoración de costo.
     * Actualiza el stock real y genera el rastro en el Kardex.
     */
    @Transactional
    public void registrarMovimientoInventario(Integer idProducto, Integer idSucursal, Integer idEmpresa,
                                              BigDecimal cantidad, BigDecimal costoUnitario, String tipoAjuste,
                                              String motivo, Integer idUsuario, String origen) {

        // 1. VALIDACIÓN DE SEGURIDAD: El producto debe existir y ser de la empresa
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + idProducto));

        if (!producto.getIdEmpresa().equals(idEmpresa)) {
            throw new RuntimeException("Error de seguridad: El producto no pertenece a su empresa.");
        }

        // 2. Crear cabecera del Ajuste (Documento de respaldo)
        InventarioAjuste ajuste = new InventarioAjuste();
        ajuste.setIdEmpresa(idEmpresa);
        ajuste.setIdSucursal(idSucursal);
        ajuste.setIdUsuario(idUsuario);
        ajuste.setTipoAjuste(tipoAjuste.toUpperCase());
        ajuste.setMotivo(motivo.toUpperCase());
        ajuste.setOrigen(origen.toUpperCase());
        ajuste.setFecha(LocalDateTime.now());
        ajuste = ajusteRepository.save(ajuste);

        // 3. Buscar o Inicializar el registro de inventario (Stock Actual)
        // Usamos el método seguro que creamos en el repositorio
        Inventario inv = inventarioRepository.findParaMovimiento(idEmpresa, idSucursal, idProducto)
                .orElseGet(() -> {
                    Inventario nuevoInv = new Inventario();
                    nuevoInv.setIdEmpresa(idEmpresa);
                    nuevoInv.setIdSucursal(idSucursal);
                    nuevoInv.setProducto(producto);
                    nuevoInv.setStockActual(BigDecimal.ZERO);
                    nuevoInv.setStockMinimo(BigDecimal.TEN); // Valor por defecto
                    return nuevoInv;
                });

        BigDecimal saldoAnterior = inv.getStockActual() != null ? inv.getStockActual() : BigDecimal.ZERO;
        BigDecimal saldoResultante;

        // 4. Calcular nuevo saldo según el tipo de movimiento
        if ("INGRESO".equalsIgnoreCase(tipoAjuste) || "ENTRADA".equalsIgnoreCase(tipoAjuste)) {
            saldoResultante = saldoAnterior.add(cantidad);
            // Si es ingreso, actualizamos el costo último en el producto para futuras ventas
            if (costoUnitario != null && costoUnitario.compareTo(BigDecimal.ZERO) > 0) {
                producto.setCostoUltimo(costoUnitario.doubleValue());
                productoRepository.save(producto);
            }
        } else {
            saldoResultante = saldoAnterior.subtract(cantidad);
        }

        // 5. Actualizar Inventario Maestro
        inv.setStockActual(saldoResultante);
        inventarioRepository.save(inv);

        // 6. Registrar movimiento detallado en el Kardex
        InventarioKardex kardex = new InventarioKardex();
        kardex.setIdEmpresa(idEmpresa);
        kardex.setIdSucursal(idSucursal);
        kardex.setInventario(inv);
        kardex.setInventarioAjuste(ajuste);
        kardex.setTipoMovimiento("AJUSTE_" + tipoAjuste.toUpperCase());
        kardex.setCantidad(cantidad);
        kardex.setCostoUnitario(costoUnitario != null ? costoUnitario : BigDecimal.ZERO);
        kardex.setSaldoAnterior(saldoAnterior);
        kardex.setSaldoResultante(saldoResultante);
        kardex.setIdUsuario(idUsuario);
        kardex.setComentario(motivo.toUpperCase());
        kardex.setFecha(LocalDateTime.now());

        kardexRepository.save(kardex);
    }

    /**
     * Obtiene los productos con stock bajo filtrados por empresa.
     */
    public List<Inventario> obtenerStockBajo(Integer idEmpresa) {
        return inventarioRepository.findFaltantesCriticos(idEmpresa);
    }
}