package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "banco")
public class Banco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_banco")
    private Integer idBanco;

    @Column(name = "nombre_banco")
    private String nombreBanco;

    private Integer activo; // 1 para activo, 0 para inactivo

    @Column(name = "id_empresa")
    private Integer idEmpresa; // Nuevo campo para filtrar por negocio

    // Getters y Setters
    public Integer getIdBanco() { return idBanco; }
    public void setIdBanco(Integer idBanco) { this.idBanco = idBanco; }

    public String getNombreBanco() { return nombreBanco; }
    public void setNombreBanco(String nombreBanco) { this.nombreBanco = nombreBanco; }

    public Integer getActivo() { return activo; }
    public void setActivo(Integer activo) { this.activo = activo; }

    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }
}