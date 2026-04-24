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

    // --- NÚCLEO DE TRAZABILIDAD (Senior Approach) ---

    @Column(name = "tipo_referencia", length = 20)
    private String tipoReferencia; // "VENTA", "GASTO", "COMPRA", "APERTURA", "AJUSTE"

    @Column(name = "id_referencia")
    private Integer idReferencia; // El ID de la venta, el gasto o la compra

    @Column(name = "id_forma_pago")
    private Integer idFormaPago; // FK a la tabla forma_pago (1=Efectivo, 2=Tarjeta, etc.)

    @Column(name = "tipo_movimiento", length = 20)
    private String tipoMovimiento; // "INGRESO" o "EGRESO"

    @Column(precision = 12, scale = 2)
    private BigDecimal monto;

    private String descripcion;

    @Column(name = "fecha_movimiento", updatable = false)
    private LocalDateTime fechaMovimiento;

    @PrePersist
    protected void onCreate() {
        if (this.fechaMovimiento == null) {
            this.fechaMovimiento = LocalDateTime.now();
        }
    }

    // --- CONSTRUCTOR DE CONVENIENCIA PARA EL SERVICE ---
    public CajaMovimiento() {}

    // Getters y Setters...
    public Integer getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(Integer idMovimiento) { this.idMovimiento = idMovimiento; }
    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }
    public Integer getIdSesion() { return idSesion; }
    public void setIdSesion(Integer idSesion) { this.idSesion = idSesion; }
    public String getTipoReferencia() { return tipoReferencia; }
    public void setTipoReferencia(String tipoReferencia) { this.tipoReferencia = tipoReferencia; }
    public Integer getIdReferencia() { return idReferencia; }
    public void setIdReferencia(Integer idReferencia) { this.idReferencia = idReferencia; }
    public Integer getIdFormaPago() { return idFormaPago; }
    public void setIdFormaPago(Integer idFormaPago) { this.idFormaPago = idFormaPago; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
}