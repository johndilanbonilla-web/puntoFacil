package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    // --- MÉTODOS PARA DASHBOARD (ViewController) ---

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fechaVenta >= :inicio AND v.idEmpresa = :idEmpresa AND v.estado = 'COMPLETADA'")
    BigDecimal sumTotalVentasHoyByEmpresa(@Param("inicio") LocalDateTime inicio, @Param("idEmpresa") Integer idEmpresa);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaVenta >= :inicio AND v.idEmpresa = :idEmpresa AND v.estado = 'COMPLETADA'")
    Long countVentasHoyByEmpresa(@Param("inicio") LocalDateTime inicio, @Param("idEmpresa") Integer idEmpresa);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin AND v.idEmpresa = :idEmpresa AND v.estado = 'COMPLETADA'")
    BigDecimal sumTotalVentasEntreFechasByEmpresa(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("idEmpresa") Integer idEmpresa);


    // --- MÉTODOS DE LISTADO (CORREGIDO PARA EVITAR CANNOT FIND SYMBOL) ---

    /**
     * Este nombre coincide exactamente con la línea 116 de tu ViewController.java
     */
    @Query("SELECT v FROM Venta v WHERE v.idEmpresa = :idEmpresa AND v.estado != 'ELIMINADA' ORDER BY v.fechaVenta DESC")
    List<Venta> findAllByIdEmpresaOrderByFechaVentaDesc(@Param("idEmpresa") Integer idEmpresa);

    /**
     * Versión alternativa por si se usa en otros componentes
     */
    @Query("SELECT v FROM Venta v WHERE v.idEmpresa = :idEmpresa AND v.estado != 'ELIMINADA' ORDER BY v.fechaVenta DESC")
    List<Venta> findAllByIdEmpresaOrderByFechaDesc(@Param("idEmpresa") Integer idEmpresa);


    // --- BÚSQUEDA CON FILTROS ---

    @Query("SELECT v FROM Venta v WHERE v.idEmpresa = :idEmpresa " +
            "AND (:fInicio IS NULL OR v.fechaVenta >= :fInicio) " +
            "AND (:fFin IS NULL OR v.fechaVenta <= :fFin) " +
            "AND (:cliente IS NULL OR v.nombreCliente LIKE %:cliente%) " +
            "ORDER BY v.fechaVenta DESC")
    List<Venta> buscarConFiltros(
            @Param("idEmpresa") Integer idEmpresa,
            @Param("fInicio") LocalDateTime fInicio,
            @Param("fFin") LocalDateTime fFin,
            @Param("cliente") String cliente
    );


    // --- CONSULTAS DETALLADAS ---

    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto WHERE v.idVenta = :id")
    Optional<Venta> findByIdConDetalles(@Param("id") Integer id);


    // --- REPORTES NATIVOS ---

    @Query(value = """
        SELECT 
            fp.nombre AS metodo, 
            SUM(vp.monto) AS total,
            fp.tipo_categoria AS categoria
        FROM venta_pago vp
        JOIN venta v ON vp.id_venta = v.id_venta
        JOIN forma_pago fp ON vp.id_forma_pago = fp.id_forma_pago
        WHERE v.id_sesion = :idSesion AND v.estado = 'COMPLETADA'
        GROUP BY fp.id_forma_pago, fp.nombre, fp.tipo_categoria
    """, nativeQuery = true)
    List<Map<String, Object>> obtenerResumenPagosPorSesion(@Param("idSesion") Integer idSesion);
}