package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.Banco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BancoRepository extends JpaRepository<Banco, Integer> {

    // Busca todos los bancos de una empresa
    List<Banco> findByIdEmpresa(Integer idEmpresa);

    // Busca bancos activos de una empresa ordenados por nombre
    // Nota: Usamos NombreBanco para coincidir con el atributo de tu Entity
    List<Banco> findByIdEmpresaAndActivoOrderByNombreBancoAsc(Integer idEmpresa, Integer activo);
}