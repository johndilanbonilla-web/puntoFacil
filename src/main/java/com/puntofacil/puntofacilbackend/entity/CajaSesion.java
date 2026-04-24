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

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "id_sucursal")
    private Integer idSucursal;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_caja")
    private Integer idCaja;

    @Column(name = "fecha_apertura")
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    // --- REFACTORIZACIÓN FINANCIERA (BigDecimal) ---

    @Column(name = "monto_apertura", precision = 12, scale = 2, nullable = false)
    private BigDecimal montoApertura = BigDecimal.ZERO;

    @Column(name = "monto_cierre_real", precision = 12, scale = 2)
    private BigDecimal montoCierreReal;

    @Column(name = "monto_cierre_sistema", precision = 12, scale = 2)
    private BigDecimal montoCierreSistema;

    private String estado; // "ABIERTA", "CERRADA"

    // --- CAMPOS TRANSIENTES (Para la Vista Thymeleaf) ---
    // Inicializados en ZERO para evitar NullPointerExceptions en la vista

    @Transient
    private BigDecimal totalVentas = BigDecimal.ZERO;

    @Transient
    private BigDecimal totalGastos = BigDecimal.ZERO;

    // --- GETTERS Y SETTERS ---

    public Integer getIdSesion() { return idSesion; }
    public void setIdSesion(Integer idSesion) { this.idSesion = idSesion; }

    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public Integer getIdSucursal() { return idSucursal; }
    public void setIdSucursal(Integer idSucursal) { this.idSucursal = idSucursal; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdCaja() { return idCaja; }
    public void setIdCaja(Integer idCaja) { this.idCaja = idCaja; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public BigDecimal getMontoApertura() { return montoApertura; }
    public void setMontoApertura(BigDecimal montoApertura) { this.montoApertura = montoApertura; }

    public BigDecimal getMontoCierreReal() { return montoCierreReal; }
    public void setMontoCierreReal(BigDecimal montoCierreReal) { this.montoCierreReal = montoCierreReal; }

    public BigDecimal getMontoCierreSistema() { return montoCierreSistema; }
    public void setMontoCierreSistema(BigDecimal montoCierreSistema) { this.montoCierreSistema = montoCierreSistema; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // --- GETTERS Y SETTERS TRANSIENTES ---

    public BigDecimal getTotalVentas() { return totalVentas; }
    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas != null ? totalVentas : BigDecimal.ZERO;
    }

    public BigDecimal getTotalGastos() { return totalGastos; }
    public void setTotalGastos(BigDecimal totalGastos) {
        this.totalGastos = totalGastos != null ? totalGastos : BigDecimal.ZERO;
    }

    /**
     * Calcula la diferencia esperada (Ventas - Gastos)
     * Utiliza los métodos nativos de BigDecimal para operaciones matemáticas seguras.
     */
    public BigDecimal getDiferenciaCalculada() {
        return this.totalVentas.subtract(this.totalGastos);
    }
}