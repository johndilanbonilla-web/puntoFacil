package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "venta_detalle")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    // --- AGREGAR ESTE CAMPO ---
    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta")
    @JsonIgnore
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad")
    private Double cantidad;

    @Column(name = "precio_unitario")
    private Double precioUnitario;

    @Column(name = "costo_historico")
    private Double costoHistorico;

    @Column(name = "impuesto_monto")
    private Double impuestoMonto;

    // --- CONSTRUCTORES ---
    public DetalleVenta() {}

    // --- GETTERS Y SETTERS ---

    // Getter y Setter para idEmpresa
    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer idDetalle) { this.idDetalle = idDetalle; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }

    public Double getCostoHistorico() { return costoHistorico; }
    public void setCostoHistorico(Double costoHistorico) { this.costoHistorico = costoHistorico; }

    public Double getImpuestoMonto() { return impuestoMonto; }
    public void setImpuestoMonto(Double impuestoMonto) { this.impuestoMonto = impuestoMonto; }

    // --- LÓGICA DE NEGOCIO ---
    public Double getSubtotal() {
        return (cantidad != null && precioUnitario != null) ? cantidad * precioUnitario : 0.0;
    }

    public Integer getIdProducto() {
        return (producto != null) ? producto.getIdProducto() : null;
    }
}