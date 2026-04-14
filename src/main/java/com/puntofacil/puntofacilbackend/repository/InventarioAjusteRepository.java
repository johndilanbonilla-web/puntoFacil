package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.InventarioAjuste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioAjusteRepository extends JpaRepository<InventarioAjuste, Integer> {

    /**
     * Busca todos los ajustes manuales realizados por una empresa específica.
     * Útil para auditorías de quién movió el inventario y por qué.
     */
    List<InventarioAjuste> findByIdEmpresaOrderByIdAjusteDesc(Integer idEmpresa);

    /**
     * Busca ajustes realizados por un usuario específico dentro de una empresa.
     */
    List<InventarioAjuste> findByIdEmpresaAndIdUsuarioOrderByIdAjusteDesc(Integer idEmpresa, Integer idUsuario);

    /**
     * Consulta para obtener el resumen de ajustes de un producto específico.
     * (Asumiendo que el ajuste tiene relación o referencia al producto).
     */
    @Query("SELECT a FROM InventarioAjuste a WHERE a.idEmpresa = :idEmpresa AND a.motivo LIKE %:termino%")
    List<InventarioAjuste> buscarPorMotivo(@Param("idEmpresa") Integer idEmpresa, @Param("termino") String termino);
}