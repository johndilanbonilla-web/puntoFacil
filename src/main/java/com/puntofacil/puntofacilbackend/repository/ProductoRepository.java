package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    /**
     * BÚSQUEDA PAGINADA (MANTENIMIENTO)
     * Soporta filtro por query (nombre/barras/categoría) y filtro opcional por tipo.
     */
    @Query("SELECT p FROM Producto p " +
            "LEFT JOIN p.familia f " +
            "LEFT JOIN f.categoria c " +
            "WHERE p.idEmpresa = :idEmpresa " +
            "AND p.activo = true " +
            "AND (:tipo IS NULL OR p.idTipoProducto = :tipo) " + // Filtro opcional de tipo
            "AND (:q IS NULL OR :q = '' " +
            "  OR UPPER(p.nombre) LIKE UPPER(CONCAT('%', :q, '%')) " +
            "  OR p.codigoBarra LIKE CONCAT('%', :q, '%') " +
            "  OR UPPER(c.nombre) LIKE UPPER(CONCAT('%', :q, '%')))")
    Page<Producto> buscarPaginado(@Param("q") String q,
                                  @Param("idEmpresa") Integer idEmpresa,
                                  @Param("tipo") Integer tipo,
                                  Pageable pageable);

    /**
     * BÚSQUEDA POS
     */
    @Query("SELECT p FROM Producto p " +
            "LEFT JOIN p.familia f " +
            "LEFT JOIN f.categoria c " +
            "WHERE p.idEmpresa = :idEmpresa " +
            "AND p.activo = true " +
            "AND (:q IS NULL OR :q = '' " +
            "  OR UPPER(p.nombre) LIKE UPPER(CONCAT('%', :q, '%')) " +
            "  OR p.codigoBarra LIKE CONCAT('%', :q, '%'))")
    List<Producto> buscar(@Param("q") String q, @Param("idEmpresa") Integer idEmpresa);

    /**
     * MÉTODO REQUERIDO POR INVENTARIOCONTROLLER
     */
    List<Producto> findByActivoTrueAndIdEmpresa(Integer idEmpresa);

    /**
     * RANKING DE VENTAS
     */
    @Query(value = """
        SELECT p.* FROM producto p 
        JOIN venta_detalle dv ON p.id_producto = dv.id_producto 
        WHERE p.id_empresa = :idEmpresa AND p.activo = 1 
        GROUP BY p.id_producto 
        ORDER BY SUM(dv.cantidad) DESC 
        LIMIT 10
    """, nativeQuery = true)
    List<Producto> findTopVendidos(@Param("idEmpresa") Integer idEmpresa);

    /**
     * INDICADOR DASHBOARD: STOCK BAJO
     */
    @Query("SELECT COUNT(p) FROM Producto p " +
            "JOIN p.inventario i " +
            "WHERE p.idEmpresa = :idEmpresa " +
            "AND p.activo = true " +
            "AND i.stockActual <= i.stockMinimo")
    Long countStockBajo(@Param("idEmpresa") Integer idEmpresa);

    // --- MÉTODOS DE VALIDACIÓN ---

    List<Producto> findByEsFavoritoTrueAndIdEmpresaAndActivoTrue(Integer idEmpresa);

    boolean existsByNombreIgnoreCaseAndIdEmpresa(String nombre, Integer idEmpresa);

    boolean existsByCodigoBarraAndIdEmpresa(String codigoBarra, Integer idEmpresa);

    Optional<Producto> findByCodigoBarraAndIdEmpresaAndActivoTrue(String codigoBarra, Integer idEmpresa);
}