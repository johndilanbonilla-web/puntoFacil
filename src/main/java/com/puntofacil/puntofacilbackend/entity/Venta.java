package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer idVenta;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "id_sucursal", nullable = false)
    private Integer idSucursal;

    @Column(name = "id_sesion", nullable = false)
    private Integer idSesion;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    // --- RELACIÓN CON CLIENTE ---
    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente; // Mantenemos el ID para compatibilidad y rapidez

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", insertable = false, updatable = false)
    private Cliente cliente; // Objeto Cliente para navegar la relación

    @Column(name = "nombre_cliente")
    private String nombreCliente; // <--- AQUÍ SE GUARDA EL NOMBRE TEMPORAL (ej: Jose Rodriguez)
    // ----------------------------

    @Column(name = "id_tipo_doc", nullable = false)
    private Integer idTipoDoc;

    @Column(name = "id_correlativo", nullable = false)
    private Integer idCorrelativo;

    @Column(name = "numero_documento")
    private Integer numeroDocumento;

    @Column(name = "total", precision = 12, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "fecha_venta")
    private LocalDateTime fechaVenta;

    @Column(name = "estado")
    private String estado;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", insertable = false, updatable = false)
    private List<VentaPago> pagos = new ArrayList<>();

    // --- MÉTODOS DE CONVENIENCIA ---
    public void addDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }

    // --- GETTERS Y SETTERS ---
    public Integer getIdVenta() { return idVenta; }
    public void setIdVenta(Integer idVenta) { this.idVenta = idVenta; }

    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public Integer getIdSucursal() { return idSucursal; }
    public void setIdSucursal(Integer idSucursal) { this.idSucursal = idSucursal; }

    public Integer getIdSesion() { return idSesion; }
    public void setIdSesion(Integer idSesion) { this.idSesion = idSesion; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public Integer getIdTipoDoc() { return idTipoDoc; }
    public void setIdTipoDoc(Integer idTipoDoc) { this.idTipoDoc = idTipoDoc; }

    public Integer getIdCorrelativo() { return idCorrelativo; }
    public void setIdCorrelativo(Integer idCorrelativo) { this.idCorrelativo = idCorrelativo; }

    public Integer getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(Integer numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }

    public List<VentaPago> getPagos() { return pagos; }
    public void setPagos(List<VentaPago> pagos) { this.pagos = pagos; }
}