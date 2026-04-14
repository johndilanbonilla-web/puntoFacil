package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.CajaSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface CajaSesionRepository extends JpaRepository<CajaSesion, Integer> {

    /**
     * MÉTODO REQUERIDO POR VIEWCONTROLLER:
     * Busca la sesión abierta para un usuario específico dentro de su empresa.
     */
    Optional<CajaSesion> findByEstadoAndIdUsuarioAndIdEmpresa(String estado, Integer idUsuario, Integer idEmpresa);

    /**
     * ALIAS PARA COMPATIBILIDAD (Si el Controller solo envía Estado e Usuario):
     * Nota: Es recomendable usar siempre el que incluye idEmpresa.
     */
    Optional<CajaSesion> findByEstadoAndIdUsuario(String estado, Integer idUsuario);

    /**
     * SESIÓN ACTIVA POR EMPRESA:
     * El método más importante para el POS y el Cierre de Caja.
     */
    Optional<CajaSesion> findByIdEmpresaAndEstado(Integer idEmpresa, String estado);

    /**
     * VALIDACIÓN DE APERTURA:
     * Verifica si ya hay una caja abierta en esta empresa.
     */
    boolean existsByIdEmpresaAndEstado(Integer idEmpresa, String estado);

    /**
     * HISTORIAL BÁSICO:
     * Solo muestra las sesiones que pertenecen al negocio logueado.
     */
    List<CajaSesion> findByIdEmpresaOrderByFechaAperturaDesc(Integer idEmpresa);

    /**
     * BÚSQUEDA AVANZADA CON FILTROS:
     */
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

    /**
     * BÚSQUEDA POR SUCURSAL Y EMPRESA:
     */
    Optional<CajaSesion> findByIdEmpresaAndIdSucursalAndEstado(Integer idEmpresa, Integer idSucursal, String estado);
}