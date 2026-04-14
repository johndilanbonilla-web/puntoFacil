package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "id_empresa", nullable = false)
    private Long idEmpresa;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Campo de compatibilidad: Se llena automáticamente en el Service
     * con el valor de nombre_rol de la tabla Roles.
     */
    @Column(name = "rol")
    private String rol;

    @Column(name = "activo")
    private Boolean activo = true;

    /**
     * Relación con la tabla Roles (id_role)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_role")
    private Rol roleRelacional;

    /**
     * Relación con la tabla Sucursal (id_sucursal)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;
}