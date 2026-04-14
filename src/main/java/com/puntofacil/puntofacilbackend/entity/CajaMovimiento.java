package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "caja_movimiento")
public class CajaMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMovimiento;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "id_sesion", nullable = false)
    private Integer idSesion;

    // --- CAMPOS DE TRAZABILIDAD (Nuevos) ---
    @Column(name = "id_venta")
    private Integer idVenta; // Permite saber qué ticket originó el movimiento

    @Column(name = "id_venta_pago")
    private Integer idVentaPago; // Enlace directo al desglose en venta_pago

    @Column(name = "tipo_movimiento", length = 20)
    private String tipoMovimiento; // "INGRESO" o "EGRESO"

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago; // "EFECTIVO", "TARJETA", etc.

    @Column(precision = 12, scale = 2)
    private BigDecimal monto;

    private String descripcion;

    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;

    @PrePersist
    protected void onCreate() {
        if (this.fechaMovimiento == null) {
            this.fechaMovimiento = LocalDateTime.now();
        }
    }

    // --- GETTERS Y SETTERS ---

    public Integer getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(Integer idMovimiento) { this.idMovimiento = idMovimiento; }

    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public Integer getIdSesion() { return idSesion; }
    public void setIdSesion(Integer idSesion) { this.idSesion = idSesion; }

    public Integer getIdVenta() { return idVenta; }
    public void setIdVenta(Integer idVenta) { this.idVenta = idVenta; }

    public Integer getIdVentaPago() { return idVentaPago; }
    public void setIdVentaPago(Integer idVentaPago) { this.idVentaPago = idVentaPago; }

    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
}