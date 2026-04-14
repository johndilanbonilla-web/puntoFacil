package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "venta_pago")
public class VentaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta_pago")
    private Integer idPago;

    @Column(name = "id_venta")
    private Integer idVenta;

    @Column(name = "id_forma_pago")
    private Integer idFormaPago;

    @Column(name = "monto")
    private BigDecimal monto;

    @Column(name = "referencia")
    private String referencia;

    // --- ADAPTACIÓN AQUÍ: Cambiamos String banco por Integer id_banco ---
    @Column(name = "id_banco")
    private Integer idBanco;

    // Getters y Setters
    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }

    public Integer getIdVenta() { return idVenta; }
    public void setIdVenta(Integer idVenta) { this.idVenta = idVenta; }

    public Integer getIdFormaPago() { return idFormaPago; }
    public void setIdFormaPago(Integer idFormaPago) { this.idFormaPago = idFormaPago; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public Integer getIdBanco() { return idBanco; }
    public void setIdBanco(Integer idBanco) { this.idBanco = idBanco; }
}