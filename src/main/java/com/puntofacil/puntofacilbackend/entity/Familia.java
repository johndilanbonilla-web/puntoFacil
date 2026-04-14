package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "familia")
public class Familia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_familia")
    private Integer idFamilia;

    @Column(name = "id_empresa")
    private Integer idEmpresa;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "activo")
    private Boolean activo = true;

    // --- CAMBIO CLAVE: Relación de objeto en lugar de solo el ID ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria") // Hibernate usará esta columna para el JOIN
    private Categoria categoria;

    // --- CONSTRUCTORES ---
    public Familia() {}

    // --- GETTERS Y SETTERS ---
    public Integer getIdFamilia() { return idFamilia; }
    public void setIdFamilia(Integer idFamilia) { this.idFamilia = idFamilia; }

    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}