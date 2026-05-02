package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.CajaMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface ReporteRepository extends JpaRepository<CajaMovimiento, Long> {

    @Query(value = "SELECT " +
            "  v.fecha_venta AS fecha_venta, " +
            "  v.numero_documento AS numero_documento, " +
            "  q.nombre AS tipo_documento, " +
            "  p.nombre AS producto, " +
            "  SUM(d.cantidad) AS total_vendido, " +
            "  SUM(d.cantidad * d.precio_unitario) AS total_ventas " +
            "FROM venta v " +
            "INNER JOIN venta_detalle d ON v.id_venta = d.id_venta " +
            "INNER JOIN producto p ON d.id_producto = p.id_producto " +
            "INNER JOIN tipo_documento q ON v.id_tipo_doc = q.id_tipo_doc " +
            "WHERE v.estado = 'COMPLETADA' " +
            "  AND v.fecha_venta BETWEEN :inicio AND :fin " +
            "  AND v.id_empresa = :idEmpresa " + // <-- Parametrizado
            "  AND v.id_sucursal = :idSucursal " + // <-- Parametrizado
            "GROUP BY p.nombre, v.fecha_venta, v.numero_documento, q.nombre " +
            "ORDER BY total_ventas DESC, v.fecha_venta DESC",
            nativeQuery = true)
    List<Map<String, Object>> ventasPorProducto(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin,
            @Param("idEmpresa") Integer idEmpresa,
            @Param("idSucursal") Integer idSucursal
    );


    // 1. QUERY: Detalle de Ventas (Para la tabla)
    @Query(value = "SELECT " +
            "    v.fecha_venta AS fecha_venta, " +
            "    v.numero_documento AS numero_documento, " +
            "    IF(v.id_cliente = 1, v.nombre_cliente, c.nombre) AS cliente, " +
            "    q.nombre AS tipo_doc, " +
            "    CASE WHEN COUNT(DISTINCT f.id_forma_pago) > 1 THEN 'COMBINADO' ELSE MAX(f.nombre) END AS forma_pago, " +
            "    (t.total_vendido * q.afecta_signo) AS total_vendido, " +
            "    (t.total_ventas * q.afecta_signo) AS total_ventas " +
            "FROM venta v " +
            "INNER JOIN cliente c ON c.id_cliente = v.id_cliente " +
            "INNER JOIN tipo_documento q ON v.id_tipo_doc = q.id_tipo_doc " +
            "INNER JOIN ( " +
            "    SELECT d.id_venta, SUM(d.cantidad) AS total_vendido, SUM(d.cantidad * d.precio_unitario) AS total_ventas " +
            "    FROM venta_detalle d GROUP BY d.id_venta " +
            ") t ON t.id_venta = v.id_venta " +
            "LEFT JOIN venta_pago p ON p.id_venta = v.id_venta " +
            "LEFT JOIN forma_pago f ON f.id_forma_pago = p.id_forma_pago " +
            "WHERE v.estado = 'COMPLETADA' " +
            "AND v.fecha_venta BETWEEN :inicio AND :fin " +
            "AND v.id_empresa = :idEmpresa " +
            "AND v.id_sucursal = :idSucursal " +
            "GROUP BY v.id_venta " +
            "ORDER BY v.fecha_venta DESC, v.numero_documento DESC", nativeQuery = true)
    List<Map<String, Object>> ventasDetalleMes(
            @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin,
            @Param("idEmpresa") Integer idEmpresa, @Param("idSucursal") Integer idSucursal);


    // 2. QUERY: Agrupado para la Gráfica
    @Query(value = "SELECT " +
            "    DATE_FORMAT(v.fecha_venta, '%Y-%m') AS mes, " +
            "    SUM(t.total_ventas * q.afecta_signo) AS total_ventas " +
            "FROM venta v " +
            "INNER JOIN tipo_documento q ON v.id_tipo_doc = q.id_tipo_doc " +
            "INNER JOIN ( " +
            "    SELECT id_venta, SUM(cantidad * precio_unitario) total_ventas " +
            "    FROM venta_detalle GROUP BY id_venta " +
            ") t ON t.id_venta = v.id_venta " +
            "WHERE v.estado = 'COMPLETADA' " +
            "AND v.fecha_venta BETWEEN :inicio AND :fin " +
            "AND v.id_empresa = :idEmpresa " +
            "AND v.id_sucursal = :idSucursal " +
            "GROUP BY mes ORDER BY mes ASC", nativeQuery = true)
    List<Map<String, Object>> ventasAgrupadasMes(
            @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin,
            @Param("idEmpresa") Integer idEmpresa, @Param("idSucursal") Integer idSucursal);
}