package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "inventario", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_sucursal", "id_producto"})
})
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Integer idInventario;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    /**
     * CORRECCIÓN CRÍTICA:
     * Tenías 'private Integer idUsuario' mapeado a 'id_sucursal'.
     * Esto es confuso y puede causar errores en los JPQL.
     */
    @Column(name = "id_sucursal", nullable = false)
    private Integer idSucursal;

    @OneToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "stock_actual", precision = 12, scale = 3)
    private BigDecimal stockActual;

    @Column(name = "stock_minimo", precision = 12, scale = 3)
    private BigDecimal stockMinimo;

    @Column(name = "stock_maximo", precision = 12, scale = 3)
    private BigDecimal stockMaximo;

    @Version
    @Column(name = "version")
    private Integer version;

    // --- CONSTRUCTORES ---

    public Inventario() {
        this.stockActual = BigDecimal.ZERO;
        this.stockMinimo = BigDecimal.ZERO;
        this.stockMaximo = BigDecimal.ZERO;
        this.version = 0;
    }

    // --- GETTERS Y SETTERS ---

    public Integer getIdInventario() { return idInventario; }
    public void setIdInventario(Integer idInventario) { this.idInventario = idInventario; }

    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public Integer getIdSucursal() { return idSucursal; }
    public void setIdSucursal(Integer idSucursal) { this.idSucursal = idSucursal; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public BigDecimal getStockActual() { return stockActual; }
    public void setStockActual(BigDecimal stockActual) { this.stockActual = stockActual; }

    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo; }

    public BigDecimal getStockMaximo() { return stockMaximo; }
    public void setStockMaximo(BigDecimal stockMaximo) { this.stockMaximo = stockMaximo; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}