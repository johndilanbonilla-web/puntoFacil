package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Integer> {

    /**
     * BUSQUEDA DE STOCK:
     * Localiza el registro de inventario asegurando que pertenezca a la empresa y sucursal correctas.
     */
    @Query("SELECT i FROM Inventario i WHERE i.idEmpresa = :idEmpresa " +
            "AND i.idSucursal = :idSucursal AND i.producto.idProducto = :idProducto")
    Optional<Inventario> findParaMovimiento(
            @Param("idEmpresa") Integer idEmpresa,
            @Param("idSucursal") Integer idSucursal,
            @Param("idProducto") Integer idProducto
    );

    /**
     * ALERTAS DE DASHBOARD:
     * Compara stockActual contra stockMinimo.
     */
    @Query("SELECT i FROM Inventario i WHERE i.idEmpresa = :idEmpresa AND i.stockActual <= i.stockMinimo")
    List<Inventario> findByStockBajo(@Param("idEmpresa") Integer idEmpresa);

    /**
     * REPORTE DE FALTANTES CRÍTICOS:
     * Optimizado con JOIN FETCH para evitar múltiples consultas al mostrar nombres de productos.
     * Solo incluye productos que sigan marcados como 'activos'.
     */
    @Query("SELECT i FROM Inventario i " +
            "JOIN FETCH i.producto p " +
            "WHERE i.idEmpresa = :idEmpresa " +
            "AND i.stockActual <= i.stockMinimo " +
            "AND p.activo = true " +
            "ORDER BY i.stockActual ASC")
    List<Inventario> findFaltantesCriticos(@Param("idEmpresa") Integer idEmpresa);

    /**
     * VALORIZACIÓN DE INVENTARIO:
     * Suma el valor total del inventario (Precio * Stock) para la empresa.
     */
    @Query("SELECT SUM(i.stockActual * p.precio) FROM Inventario i JOIN i.producto p " +
            "WHERE i.idEmpresa = :idEmpresa AND p.activo = true")
    Double obtenerValorTotalInventario(@Param("idEmpresa") Integer idEmpresa);
}