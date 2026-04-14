package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.CajaMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CajaMovimientoRepository extends JpaRepository<CajaMovimiento, Integer> {

    /**
     * DETALLE DE SESIÓN:
     * Lista todos los movimientos (Entradas/Salidas/Ventas) de una sesión.
     */
    List<CajaMovimiento> findByIdSesionOrderByFechaMovimientoDesc(Integer idSesion);

    /**
     * SUMATORIA PARA ARQUEO:
     * Calcula el total de INGRESOS o EGRESOS para comparar contra el monto real.
     * Usamos Double para evitar problemas de compatibilidad con funciones agregadas de SQL.
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0.0) FROM CajaMovimiento m " +
            "WHERE m.idSesion = :idSesion AND m.tipoMovimiento = :tipo")
    Double sumMontoByIdSesionAndTipo(@Param("idSesion") Integer idSesion, @Param("tipo") String tipo);

    /**
     * RESUMEN DE PAGOS:
     * Cruza los ingresos por su descripción (Ej: 'VENTA EFECTIVO', 'VENTA TARJETA').
     * Esto es lo que alimenta el gráfico o tabla de métodos de pago en el detalle de caja.
     */
    @Query("SELECT m.descripcion AS nombre, SUM(m.monto) AS total " +
            "FROM CajaMovimiento m " +
            "WHERE m.idSesion = :idSesion AND m.tipoMovimiento = 'INGRESO' " +
            "GROUP BY m.descripcion")
    List<Map<String, Object>> obtenerResumenVentasPorMetodo(@Param("idSesion") Integer idSesion);

    /**
     * SEGURIDAD EXTRA (Opcional):
     * Busca movimientos asegurando que la sesión pertenezca a la empresa.
     */
    @Query("SELECT m FROM CajaMovimiento m JOIN CajaSesion s ON m.idSesion = s.idSesion " +
            "WHERE s.idSesion = :idSesion AND s.idEmpresa = :idEmpresa")
    List<CajaMovimiento> findBySesionAndEmpresa(@Param("idSesion") Integer idSesion, @Param("idEmpresa") Integer idEmpresa);
}