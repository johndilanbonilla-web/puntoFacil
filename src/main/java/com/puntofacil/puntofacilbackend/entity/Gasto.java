package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "gasto")
@Data
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gasto")
    private Integer idGasto;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "id_producto_gasto", nullable = false)
    private Integer idProductoGasto;

    @Column(name = "id_forma_pago", nullable = false)
    private Integer idFormaPago;

    @Column(name = "id_banco")
    private Integer idBanco;

    @Column(name = "monto", nullable = false)
    private Double monto;

    @Column(name = "descripcion_personalizada")
    private String descripcionPersonalizada;

    @Column(name = "fecha_gasto")
    private LocalDateTime fechaGasto;

    /**
     * CAMBIO: De Boolean activo a String estado.
     * Valores sugeridos: "ACTIVO", "ANULADO"
     */
    @Column(name = "estado", length = 20)
    private String estado = "ACTIVO";

    @Column(name = "id_usuario_anula")
    private Integer idUsuarioAnula;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    /**
     * Helper para visualización en Thymeleaf.
     */
    public String getFechaFormateada() {
        if (this.fechaGasto == null) return "---";
        return this.fechaGasto.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Estandarización antes de insertar en la BD.
     */
    @PrePersist
    protected void onCreate() {
        if (this.fechaGasto == null) {
            this.fechaGasto = LocalDateTime.now();
        }

        // Aseguramos que el estado inicial sea ACTIVO en texto
        if (this.estado == null) {
            this.estado = "ACTIVO";
        }

        if (this.descripcionPersonalizada != null) {
            this.descripcionPersonalizada = this.descripcionPersonalizada.toUpperCase();
        }
    }
}