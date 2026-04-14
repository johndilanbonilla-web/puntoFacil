package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.CorrelativoDocumento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorrelativoRepository extends JpaRepository<CorrelativoDocumento, Integer> {

    /**
     * Busca el correlativo activo filtrando por Empresa, Sucursal y Tipo de Documento.
     * Mantenemos el Lock PESSIMISTIC_WRITE para garantizar que el "+1" sea único
     * incluso si hay múltiples cajas cobrando al mismo tiempo.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CorrelativoDocumento c " +
            "WHERE c.idEmpresa = :idEmpresa " +
            "AND c.idSucursal = :idSucursal " +
            "AND c.idTipoDoc = :idTipoDoc " +
            "AND c.activo = 1")
    Optional<CorrelativoDocumento> findActual(
            @Param("idEmpresa") Integer idEmpresa,
            @Param("idSucursal") Integer idSucursal,
            @Param("idTipoDoc") Integer idTipoDoc
    );
}