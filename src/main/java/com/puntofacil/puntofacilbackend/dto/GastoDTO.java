package com.puntofacil.puntofacilbackend.dto;

import lombok.Data;

@Data
public class GastoDTO {
    private Integer idProducto;      // El "Concepto" (tipo_producto = 4)
    private Double monto;            // Monto manual ingresado
    private String descripcion;      // Nota manual (ej: "Recibo de luz marzo")
    private Integer idFormaPago;     // De dónde sale el dinero
    private Integer idBanco;         // Opcional si es transferencia
    private String referencia;       // Opcional (n° de comprobante)
}