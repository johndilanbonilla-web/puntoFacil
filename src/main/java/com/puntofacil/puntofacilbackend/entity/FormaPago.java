package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "forma_pago")
@Data
public class FormaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_forma_pago")
    private Integer idFormaPago;

    @Column(nullable = false, length = 50)
    private String nombre;

    /**
     * Usamos EnumType.STRING para que en la base de datos se guarde el texto
     * (ejemplo: 'EFECTIVO', 'POS') en lugar de un número.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_categoria", nullable = false)
    private TipoCategoria tipoCategoria;

    @Column(nullable = false)
    private Integer activo; // 1 para Activo, 0 para Inactivo

    // --- DEFINICIÓN DEL ENUM ---
    public enum TipoCategoria {
        EFECTIVO,
        POS,
        TRANSFERENCIA,
        CREDITO,
        BITCOIN
    }
}