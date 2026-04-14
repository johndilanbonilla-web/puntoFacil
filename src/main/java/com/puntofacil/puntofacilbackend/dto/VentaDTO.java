package com.puntofacil.puntofacilbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class VentaDTO {

    private Integer idEmpresa;
    private Integer idSucursal;
    private List<ItemDetalle> detalles;
    private List<PagoDetalle> pagos;

    private Integer idCliente;
    private Integer idTipoDoc;
    private String nombreTemporal;

    // Getters y Setters
    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }
    public Integer getIdSucursal() { return idSucursal; }
    public void setIdSucursal(Integer idSucursal) { this.idSucursal = idSucursal; }
    public List<ItemDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<ItemDetalle> detalles) { this.detalles = detalles; }
    public List<PagoDetalle> getPagos() { return pagos; }
    public void setPagos(List<PagoDetalle> pagos) { this.pagos = pagos; }
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public Integer getIdTipoDoc() { return idTipoDoc; }
    public void setIdTipoDoc(Integer idTipoDoc) { this.idTipoDoc = idTipoDoc; }
    public String getNombreTemporal() { return nombreTemporal; }
    public void setNombreTemporal(String nombreTemporal) { this.nombreTemporal = nombreTemporal; }

    public static class ItemDetalle {
        @JsonProperty("idProducto")
        private Integer idProducto;
        private Double cantidad;
        private Double precioUnitario;

        public Integer getIdProducto() { return idProducto; }
        public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
        public Double getCantidad() { return cantidad; }
        public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
        public Double getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
    }

    public static class PagoDetalle {
        @JsonProperty("idFormaPago")
        private Integer idFormaPago;
        private Double monto;
        private String referencia;

        // --- CAMBIO CLAVE AQUÍ ---
        private Integer idBanco; // Cambiado de String banco a Integer idBanco

        public PagoDetalle() {}

        public Integer getIdFormaPago() { return idFormaPago; }
        public void setIdFormaPago(Integer idFormaPago) { this.idFormaPago = idFormaPago; }
        public Double getMonto() { return monto; }
        public void setMonto(Double monto) { this.monto = monto; }
        public String getReferencia() { return referencia; }
        public void setReferencia(String referencia) { this.referencia = referencia; }

        // Getter y Setter para idBanco
        public Integer getIdBanco() { return idBanco; }
        public void setIdBanco(Integer idBanco) { this.idBanco = idBanco; }
    }
}