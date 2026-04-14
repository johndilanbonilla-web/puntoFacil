package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.Familia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FamiliaRepository extends JpaRepository<Familia, Integer> {
    List<Familia> findByIdEmpresaAndActivoTrue(Integer idEmpresa);
}