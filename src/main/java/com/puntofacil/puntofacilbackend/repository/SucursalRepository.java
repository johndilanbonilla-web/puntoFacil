package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {
    // Vital para el combo del formulario: solo muestra sucursales de la empresa actual
    List<Sucursal> findByIdEmpresaAndActivo(Long idEmpresa, Integer activo);
}