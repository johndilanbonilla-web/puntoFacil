package com.puntofacil.puntofacilbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "compra")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Integer idCompra;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "id_proveedor")
    private Integer idProveedor;

    @Column(name = "id_sucursal")
    private Integer idSucursal;

    @Column(name = "id_sesion")
    private Integer idSesion;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "num_factura_proveedor", length = 50)
    private String numFacturaProveedor;

    @Column(name = "fecha_compra")
    private LocalDateTime fechaCompra;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    // SOLUCIÓN AL ERROR: El nombre en DB es total_compra
    @Column(name = "total_compra", precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "estado", length = 20)
    private String estado;

    @Column(name = "id_usuario_anula")
    private Integer idUsuarioAnula;
}