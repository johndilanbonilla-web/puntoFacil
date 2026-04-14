package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {
    List<Marca> findByIdEmpresaAndActivoTrue(Integer idEmpresa);
}