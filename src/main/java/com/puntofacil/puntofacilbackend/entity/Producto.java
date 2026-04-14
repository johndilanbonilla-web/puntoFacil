package com.puntofacil.puntofacilbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "id_empresa")
    private Integer idEmpresa;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "codigo_barra")
    private String codigoBarra;

    @Column(name = "codigo_interno")
    private String codigoInterno;

    @Column(name = "precio")
    private Double precio;

    @Column(name = "costo_ultimo")
    private Double costoUltimo;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "es_favorito")
    private Integer esFavorito;

    /* ELIMINADO: @ManyToOne Categoria
       La categoría ahora se obtiene a través de familia.getCategoria()
    */

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_familia")
    private Familia familia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_marca")
    private Marca marca;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_unidad_venta")
    private UnidadMedida unidadVenta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_unidad_compra")
    private UnidadMedida unidadCompra;

    @Column(name = "factor_conversion", precision = 12, scale = 4)
    private BigDecimal factorConversion;

    @Column(name = "id_tipo_producto")
    private Integer idTipoProducto;

    @OneToOne(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Inventario inventario;

    // --- CONSTRUCTORES ---
    public Producto() {
        this.factorConversion = BigDecimal.ONE;
        this.activo = true;
    }

    // --- MÉTODOS DE CONVENIENCIA (Opcional) ---

    /**
     * Devuelve la categoría de forma indirecta.
     * @Transient le dice a JPA que ignore este método al mapear columnas.
     */
    @Transient
    public Categoria getCategoria() {
        return (familia != null) ? familia.getCategoria() : null;
    }

    // --- GETTERS Y SETTERS RESTANTES ---

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigoBarra() { return codigoBarra; }
    public void setCodigoBarra(String codigoBarra) { this.codigoBarra = codigoBarra; }

    public String getCodigoInterno() { return codigoInterno; }
    public void setCodigoInterno(String codigoInterno) { this.codigoInterno = codigoInterno; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Double getCostoUltimo() { return costoUltimo; }
    public void setCostoUltimo(Double costoUltimo) { this.costoUltimo = costoUltimo; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Integer getEsFavorito() { return esFavorito; }
    public void setEsFavorito(Integer esFavorito) { this.esFavorito = esFavorito; }

    public Familia getFamilia() { return familia; }
    public void setFamilia(Familia familia) { this.familia = familia; }

    public Marca getMarca() { return marca; }
    public void setMarca(Marca marca) { this.marca = marca; }

    public UnidadMedida getUnidadVenta() { return unidadVenta; }
    public void setUnidadVenta(UnidadMedida unidadVenta) { this.unidadVenta = unidadVenta; }

    public UnidadMedida getUnidadCompra() { return unidadCompra; }
    public void setUnidadCompra(UnidadMedida unidadCompra) { this.unidadCompra = unidadCompra; }

    public BigDecimal getFactorConversion() { return factorConversion; }
    public void setFactorConversion(BigDecimal factorConversion) { this.factorConversion = factorConversion; }

    public Integer getIdTipoProducto() { return idTipoProducto; }
    public void setIdTipoProducto(Integer idTipoProducto) { this.idTipoProducto = idTipoProducto; }

    public Inventario getInventario() { return inventario; }
    public void setInventario(Inventario inventario) { this.inventario = inventario; }
}