package com.puntofacil.puntofacilbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.puntofacil.puntofacilbackend.entity.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}