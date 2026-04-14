package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "caja_sesion")
public class CajaSesion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSesion;

    @Column(name = "id_empresa", nullable = false) // <--- CRÍTICO PARA MULTI-EMPRESA
    private Integer idEmpresa;

    @Column(name = "id_sucursal")
    private Integer idSucursal;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_caja")
    private Integer idCaja;

    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    @Column(precision = 12, scale = 2)
    private BigDecimal montoApertura;

    @Column(precision = 12, scale = 2)
    private BigDecimal montoCierreReal;

    @Column(precision = 12, scale = 2)
    private BigDecimal montoCierreSistema;

    private String estado; // "ABIERTA", "CERRADA"

    // --- GETTERS Y SETTERS ---

    public Integer getIdSesion() { return idSesion; }
    public void setIdSesion(Integer idSesion) { this.idSesion = idSesion; }

    public Integer getIdEmpresa() { return idEmpresa; } // <--- NUEVO
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public Integer getIdSucursal() { return idSucursal; }
    public void setIdSucursal(Integer idSucursal) { this.idSucursal = idSucursal; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }

    public BigDecimal getMontoApertura() { return montoApertura; }
    public void setMontoApertura(BigDecimal montoApertura) { this.montoApertura = montoApertura; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public BigDecimal getMontoCierreReal() { return montoCierreReal; }
    public void setMontoCierreReal(BigDecimal montoCierreReal) { this.montoCierreReal = montoCierreReal; }

    public BigDecimal getMontoCierreSistema() { return montoCierreSistema; }
    public void setMontoCierreSistema(BigDecimal montoCierreSistema) { this.montoCierreSistema = montoCierreSistema; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getIdCaja() { return idCaja; }
    public void setIdCaja(Integer idCaja) { this.idCaja = idCaja; }
}