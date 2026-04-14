package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "nombre_comercial", length = 150)
    private String nombreComercial;

    @Column(name = "id_tipo_cliente")
    private Integer idTipoCliente;

    @Column(length = 20)
    private String nit;

    @Column(length = 20)
    private String nrc;

    @Column(length = 10)
    private String dui;

    @Column(length = 150)
    private String giro;

    @Column(length = 255)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String correo;

    @Column(name = "id_departamento", length = 2)
    private String idDepartamento;

    @Column(name = "id_municipio", length = 3)
    private String idMunicipio;

    // Se mantiene como Boolean para compatibilidad con JPA/Hibernate
    @Column(nullable = false)
    private Boolean activo = true;

    // --- CONSTRUCTORES ---
    public Cliente() {}

    // --- GETTERS Y SETTERS ---

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public Integer getIdTipoCliente() { return idTipoCliente; }
    public void setIdTipoCliente(Integer idTipoCliente) { this.idTipoCliente = idTipoCliente; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getNrc() { return nrc; }
    public void setNrc(String nrc) { this.nrc = nrc; }

    public String getDui() { return dui; }
    public void setDui(String dui) { this.dui = dui; }

    public String getGiro() { return giro; }
    public void setGiro(String giro) { this.giro = giro; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(String idDepartamento) { this.idDepartamento = idDepartamento; }

    public String getIdMunicipio() { return idMunicipio; }
    public void setIdMunicipio(String idMunicipio) { this.idMunicipio = idMunicipio; }

    public Boolean getActivo() { return activo; }

    /**
     * CORRECCIÓN: El parámetro debe ser Boolean para que coincida con el atributo.
     * Si desde el exterior (JS o BD) llega un número, Spring/Hibernate
     * suele manejar la conversión si el tipo en el setter es el correcto.
     */
    public void setActivo(Boolean activo) {
        this.activo = (activo != null) ? activo : true;
    }
}