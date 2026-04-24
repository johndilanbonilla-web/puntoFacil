package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.CajaSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;

@Repository
public interface CajaSesionRepository extends JpaRepository<CajaSesion, Integer> {

    // --- ESTADO Y CONTROL ---
    boolean existsByIdEmpresaAndEstado(Integer idEmpresa, String estado);

    Optional<CajaSesion> findByIdEmpresaAndEstado(Integer idEmpresa, String estado);

    Optional<CajaSesion> findByIdEmpresaAndIdSucursalAndEstado(Integer idEmpresa, Integer idSucursal, String estado);

    Optional<CajaSesion> findByEstadoAndIdUsuarioAndIdEmpresa(String estado, Integer idUsuario, Integer idEmpresa);

    // --- HISTORIAL ---
    @Query("SELECT s FROM CajaSesion s WHERE s.idEmpresa = :idEmpresa " +
            "AND (:idSucursal IS NULL OR s.idSucursal = :idSucursal) " +
            "AND (s.fechaApertura BETWEEN :inicio AND :fin) " +
            "ORDER BY s.fechaApertura DESC")
    List<CajaSesion> buscarSesionesHistoricas(
            @Param("idEmpresa") Integer idEmpresa,
            @Param("idSucursal") Integer idSucursal,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    List<CajaSesion> findByIdEmpresaOrderByFechaAperturaDesc(Integer idEmpresa);

    // --- CÁLCULOS DE AUDITORÍA (Refactorizados) ---

    /**
     * Suma de ventas usando 'idSesion' y filtrando ventas anuladas.
     * Retorna BigDecimal para mantener la precisión financiera.
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.idSesion = :idSesion AND v.estado != 'ANULADA'")
    BigDecimal sumTotalVentasBySesion(@Param("idSesion") Integer idSesion);

    /**
     * Suma de Gastos Operativos.
     * USAMOS NATIVE QUERY: SQL puro directo a la tabla MySQL
     */
    @Query(value = "SELECT COALESCE(SUM(monto), 0) FROM gasto WHERE id_sesion = :idSesion AND estado != 'ANULADO'", nativeQuery = true)
    BigDecimal sumTotalGastosBySesion(@Param("idSesion") Integer idSesion);

    /**
     * Suma de Compras de Mercadería (Inventario).
     * USAMOS NATIVE QUERY: Asegúrate de que la columna en MySQL se llame 'total_compra' o ajusta el nombre
     */
    @Query(value = "SELECT COALESCE(SUM(total_compra), 0) FROM compra WHERE id_sesion = :idSesion AND estado != 'ANULADO'", nativeQuery = true)
    BigDecimal sumTotalComprasBySesion(@Param("idSesion") Integer idSesion);
}