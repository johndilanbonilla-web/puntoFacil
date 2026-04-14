package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Integer> {
    List<UnidadMedida> findByIdEmpresaAndActivoTrue(Integer idEmpresa);
}