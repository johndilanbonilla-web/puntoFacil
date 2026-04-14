package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.InventarioKardex;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioKardexRepository extends JpaRepository<InventarioKardex, Integer> {

    /**
     * HISTORIAL ESPECÍFICO: Trae todos los movimientos de un producto
     * validando que pertenezca a la empresa actual.
     */
    @Query("SELECT k FROM InventarioKardex k " +
            "JOIN FETCH k.inventario i " +
            "WHERE i.idInventario = :idInventario AND k.idEmpresa = :idEmpresa " +
            "ORDER BY k.fecha DESC, k.idKardex DESC")
    List<InventarioKardex> findByInventarioAndEmpresa(
            @Param("idInventario") Integer idInventario,
            @Param("idEmpresa") Integer idEmpresa
    );

    /**
     * DASHBOARD DE MOVIMIENTOS:
     * Obtiene los últimos movimientos de la empresa con carga optimizada de relaciones.
     * El JOIN FETCH evita el problema de las "N+1 consultas" en Hibernate.
     */
    @Query("SELECT k FROM InventarioKardex k " +
            "JOIN FETCH k.inventario i " +
            "JOIN FETCH i.producto p " +
            "LEFT JOIN FETCH k.inventarioAjuste a " +
            "WHERE k.idEmpresa = :idEmpresa " +
            "ORDER BY k.fecha DESC, k.idKardex DESC")
    List<InventarioKardex> findRecentMovimientos(@Param("idEmpresa") Integer idEmpresa, Pageable pageable);

    /**
     * Mantenemos tu método original pero asegurando el idEmpresa.
     * Es ideal para la tabla pequeña de "Movimientos Recientes" en la vista de Mantenimiento.
     */
    @Query("SELECT k FROM InventarioKardex k " +
            "JOIN FETCH k.inventario i " +
            "JOIN FETCH i.producto p " +
            "WHERE k.idEmpresa = :idEmpresa " +
            "ORDER BY k.fecha DESC, k.idKardex DESC")
    List<InventarioKardex> findTop10Movimientos(@Param("idEmpresa") Integer idEmpresa);

    /**
     * REPORTE POR SUCURSAL:
     * Si en el futuro escalas a múltiples sucursales por empresa.
     */
    List<InventarioKardex> findByIdEmpresaAndIdSucursalOrderByFechaDesc(Integer idEmpresa, Integer idSucursal);
}