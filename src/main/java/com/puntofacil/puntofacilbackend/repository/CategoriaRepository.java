package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    // Para llenar el select del modal de productos
    List<Categoria> findByIdEmpresa(Integer idEmpresa);

    // Si manejas un campo 'activo' en categorías
    // List<Categoria> findByIdEmpresaAndActivoTrue(Integer idEmpresa);
}