package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_kardex")
public class InventarioKardex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kardex")
    private Integer idKardex;

    @Column(name = "id_empresa")
    private Integer idEmpresa;

    @Column(name = "id_sucursal")
    private Integer idSucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inventario")
    private Inventario inventario;

    @Column(name = "id_venta")
    private Integer idVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ajuste")
    private InventarioAjuste inventarioAjuste;

    @Column(name = "tipo_movimiento")
    private String tipoMovimiento; // VENTA, COMPRA, AJUSTE, ANULACION

    @Column(name = "cantidad", precision = 12, scale = 3)
    private BigDecimal cantidad;

    // --- NUEVO CAMPO: Costo Unitario del movimiento ---
    @Column(name = "costo_unitario", precision = 12, scale = 3)
    private BigDecimal costoUnitario;

    @Column(name = "saldo_anterior", precision = 12, scale = 3)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_resultante", precision = 12, scale = 3)
    private BigDecimal saldoResultante;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "comentario")
    private String comentario;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    public InventarioKardex() {
        this.fecha = LocalDateTime.now();
        this.cantidad = BigDecimal.ZERO;
        this.costoUnitario = BigDecimal.ZERO;
        this.saldoAnterior = BigDecimal.ZERO;
        this.saldoResultante = BigDecimal.ZERO;
    }

    // --- GETTERS Y SETTERS ---

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public InventarioAjuste getInventarioAjuste() { return inventarioAjuste; }
    public void setInventarioAjuste(InventarioAjuste inventarioAjuste) { this.inventarioAjuste = inventarioAjuste; }

    public Integer getIdKardex() { return idKardex; }
    public void setIdKardex(Integer idKardex) { this.idKardex = idKardex; }

    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public Integer getIdSucursal() { return idSucursal; }
    public void setIdSucursal(Integer idSucursal) { this.idSucursal = idSucursal; }

    public Inventario getInventario() { return inventario; }
    public void setInventario(Inventario inventario) { this.inventario = inventario; }

    public Integer getIdVenta() { return idVenta; }
    public void setIdVenta(Integer idVenta) { this.idVenta = idVenta; }

    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getSaldoAnterior() { return saldoAnterior; }
    public void setSaldoAnterior(BigDecimal saldoAnterior) { this.saldoAnterior = saldoAnterior; }

    public BigDecimal getSaldoResultante() { return saldoResultante; }
    public void setSaldoResultante(BigDecimal saldoResultante) { this.saldoResultante = saldoResultante; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}