package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sucursal")
public class Sucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sucursal")
    private Integer idSucursal;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "activo")
    private Integer activo = 1;

    @Column(name = "validar_stock")
    private Integer validarStock = 0;

    @Column(name = "id_departamento", length = 2)
    private String idDepartamento;

    @Column(name = "id_municipio", length = 4)
    private String idMunicipio;

    @Column(name = "id_distrito", length = 6)
    private String idDistrito;


    @Column(name = "codigo_sucursal_re", length = 10)
    private String codigoSucursalRe;
}