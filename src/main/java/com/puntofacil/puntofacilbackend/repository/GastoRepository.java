package com.puntofacil.puntofacilbackend.repository;

import com.puntofacil.puntofacilbackend.entity.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal; // Necesario para el monto total
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Integer> {

    /**
     * Devuelve la lista detallada de gastos con filtros.
     */
    @Query("SELECT g FROM Gasto g WHERE g.idEmpresa = :idEmpresa " +
            "AND (:inicio IS NULL OR g.fechaGasto >= :inicio) " +
            "AND (:fin IS NULL OR g.fechaGasto <= :fin) " +
            "ORDER BY g.fechaGasto DESC")
    List<Gasto> buscarGastos(@Param("idEmpresa") Integer idEmpresa,
                             @Param("inicio") LocalDateTime inicio,
                             @Param("fin") LocalDateTime fin);

    /**
     * SUMA TOTAL DE GASTOS: Este es el método que usa el ViewController
     * para la tarjeta "Gastos Hoy" del Dashboard.
     */
    @Query("SELECT SUM(g.monto) FROM Gasto g WHERE g.idEmpresa = :idEmpresa " +
            "AND g.fechaGasto BETWEEN :inicio AND :fin")
    BigDecimal sumTotalGastosEntreFechasByEmpresa(@Param("inicio") LocalDateTime inicio,
                                                  @Param("fin") LocalDateTime fin,
                                                  @Param("idEmpresa") Integer idEmpresa);
}