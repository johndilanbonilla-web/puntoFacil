package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_ajuste")
public class InventarioAjuste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ajuste")
    private Integer idAjuste;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "id_sucursal", nullable = false)
    private Integer idSucursal;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "tipo_ajuste", nullable = false)
    private String tipoAjuste; // INGRESO o SALIDA

    @Column(name = "origen")
    private String origen; // MANUAL o CSV

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    public InventarioAjuste() {
        this.fecha = LocalDateTime.now();
        this.origen = "MANUAL";
    }

    // --- GETTERS Y SETTERS ---
    public Integer getIdAjuste() { return idAjuste; }
    public void setIdAjuste(Integer idAjuste) { this.idAjuste = idAjuste; }

    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public Integer getIdSucursal() { return idSucursal; }
    public void setIdSucursal(Integer idSucursal) { this.idSucursal = idSucursal; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getTipoAjuste() { return tipoAjuste; }
    public void setTipoAjuste(String tipoAjuste) { this.tipoAjuste = tipoAjuste; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}