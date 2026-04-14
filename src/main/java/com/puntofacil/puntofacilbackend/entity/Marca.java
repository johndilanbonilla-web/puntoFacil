package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "marca")
@Data // Si usas Lombok, si no, genera Getters y Setters
public class Marca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marca")
    private Integer idMarca;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "activo")
    private Boolean activo = true;

    public Marca() {}

    // Getters y Setters manuales si no usas Lombok
    public Integer getIdMarca() { return idMarca; }
    public void setIdMarca(Integer idMarca) { this.idMarca = idMarca; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}