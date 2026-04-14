package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    /**
     * BUSCADOR DEL POS:
     * Búsqueda inteligente por Nombre, NIT, DUI o Nombre Comercial.
     * Restringido estrictamente a la empresa logueada y clientes activos.
     */
    @Query("SELECT c FROM Cliente c WHERE c.idEmpresa = :idEmpresa " +
            "AND c.activo = true " +
            "AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(c.nombreComercial) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR c.nit LIKE CONCAT('%', :term, '%') " +
            "OR c.dui LIKE CONCAT('%', :term, '%'))")
    List<Cliente> buscarPorTermino(@Param("idEmpresa") Integer idEmpresa, @Param("term") String term);

    /**
     * LISTADO GENERAL:
     * Retorna todos los clientes activos de una empresa específica.
     */
    List<Cliente> findByIdEmpresaAndActivoTrue(Integer idEmpresa);

    /**
     * BÚSQUEDA SEGURA POR ID:
     * Asegura que el cliente pertenezca a la empresa antes de retornarlo.
     */
    Optional<Cliente> findByIdClienteAndIdEmpresa(Integer idCliente, Integer idEmpresa);

    /**
     * CLIENTE GENÉRICO:
     * Busca el Consumidor Final predeterminado de la empresa.
     */
    @Query("SELECT c FROM Cliente c WHERE c.idEmpresa = :idEmpresa " +
            "AND UPPER(c.nombre) LIKE '%CONSUMIDOR FINAL%'")
    Optional<Cliente> findGenericoByEmpresa(@Param("idEmpresa") Integer idEmpresa);

    /**
     * BÚSQUEDA POR NOMBRE EXACTO Y EMPRESA:
     * Utilizado por el Service para el método obtenerGenerico().
     */
    Optional<Cliente> findByNombreAndIdEmpresa(String nombre, Integer idEmpresa);
}