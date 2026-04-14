package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.FormaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FormaPagoRepository extends JpaRepository<FormaPago, Integer> {
    // Solo traer las que están habilitadas para el POS
    List<FormaPago> findByActivoOrderByNombreAsc(Integer activo);
}