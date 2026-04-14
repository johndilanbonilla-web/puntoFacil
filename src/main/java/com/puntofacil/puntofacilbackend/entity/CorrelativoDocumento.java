package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "correlativo_documento")
@Data
public class CorrelativoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_correlativo")
    private Integer idCorrelativo;

    @Column(name = "id_sucursal")
    private Integer idSucursal;

    @Column(name = "id_tipo_doc")
    private Integer idTipoDoc;

    private String serie;

    @Column(name = "numero_inicio")
    private Integer numeroInicio;

    @Column(name = "numero_actual")
    private Integer numeroActual;

    @Column(name = "numero_final")
    private Integer numeroFinal;

    @Column(name = "id_empresa")
    private Integer idEmpresa;

    private Integer activo; // 1 = Activo, 0 = Inactivo
}