package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.CajaMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface CajaMovimientoRepository extends JpaRepository<CajaMovimiento, Integer> {

    /**
     * Recupera todos los movimientos de una sesión específica.
     * Usado para el listado general en la vista de auditoría.
     */
    List<CajaMovimiento> findByIdSesion(Integer idSesion);

    /**
     * Versión ordenada para mostrar el historial del más reciente al más antiguo.
     */
    List<CajaMovimiento> findByIdSesionOrderByFechaMovimientoDesc(Integer idSesion);

    /**
     * Calcula totales de INGRESOS o EGRESOS.
     * Importante: Se cambió 'tipoMovimiento' a 'tipo' para coincidir con la Entity.
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM CajaMovimiento m " +
            "WHERE m.idSesion = :idSesion AND m.tipoMovimiento = :tipo")
    BigDecimal sumMontoByIdSesionAndTipo(@Param("idSesion") Integer idSesion, @Param("tipo") String tipo);


    /**
     * Resumen de Ingresos por Método de Pago (Con la nueva estructura)
     * Une con la tabla forma_pago para obtener el nombre exacto.
     */
    @Query("SELECT fp.nombre AS nombre, SUM(m.monto) AS total " +
            "FROM CajaMovimiento m " +
            "JOIN FormaPago fp ON m.idFormaPago = fp.idFormaPago " +
            "WHERE m.idSesion = :idSesion AND m.tipoMovimiento = 'INGRESO' " +
            "GROUP BY fp.nombre")
    List<Map<String, Object>> obtenerResumenVentasPorMetodo(@Param("idSesion") Integer idSesion);


    /**
     * Obtiene el total EXACTO en efectivo.
     * Asumiendo que el ID de Efectivo en tu tabla forma_pago es 1 (o filtra por fp.tipoCategoria = 'EFECTIVO')
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM CajaMovimiento m " +
            "JOIN FormaPago fp ON m.idFormaPago = fp.idFormaPago " +
            "WHERE m.idSesion = :idSesion AND m.tipoMovimiento = 'INGRESO' AND fp.tipoCategoria = 'EFECTIVO'")
    BigDecimal sumVentasEfectivoBySesion(@Param("idSesion") Integer idSesion);
}