package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.repository.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    // ================================
    // 📊 REPORTE: VENTAS POR PRODUCTO
    // ================================
    public List<Map<String, Object>> ventasPorProducto(LocalDate inicio, LocalDate fin, Integer idEmpresa, Integer idSucursal) {
        return reporteRepository.ventasPorProducto(inicio, fin, idEmpresa, idSucursal);
    }

    // ================================
    // 📅 REPORTE: VENTAS MENSUALES
    // ================================

    // 1. Detalle de facturas (para la tabla inferior)
    public List<Map<String, Object>> ventasDetalleMes(LocalDate inicio, LocalDate fin, Integer idEmpresa, Integer idSucursal) {
        return reporteRepository.ventasDetalleMes(inicio, fin, idEmpresa, idSucursal);
    }

    // 2. Agrupado por mes (para dibujar la gráfica)
    public List<Map<String, Object>> ventasAgrupadasMes(LocalDate inicio, LocalDate fin, Integer idEmpresa, Integer idSucursal) {
        return reporteRepository.ventasAgrupadasMes(inicio, fin, idEmpresa, idSucursal);
    }

}