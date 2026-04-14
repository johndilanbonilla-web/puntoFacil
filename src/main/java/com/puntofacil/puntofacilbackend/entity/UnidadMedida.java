package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "unidad_medida")
public class UnidadMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad") // <--- CORREGIDO: Coincide con tu imagen de la DB
    private Integer idUnidad;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "abreviatura", length = 10)
    private String abreviatura;

    @Column(name = "activo")
    private Boolean activo = true;

    // --- CONSTRUCTORES ---
    public UnidadMedida() {}

    public UnidadMedida(Integer idUnidad) {
        this.idUnidad = idUnidad;
    }

    // --- GETTERS Y SETTERS ---

    public Integer getIdUnidad() {
        return idUnidad;
    }

    public void setIdUnidad(Integer idUnidad) {
        this.idUnidad = idUnidad;
    }

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}