package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.dto.VentaDTO;
import com.puntofacil.puntofacilbackend.entity.*;
import com.puntofacil.puntofacilbackend.repository.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class VentaService {

    @Autowired private VentaRepository ventaRepository;
    @Autowired private CorrelativoRepository correlativoRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    // --- CONSULTAS Y FILTROS ---

    @Transactional(readOnly = true)
    public List<Venta> buscarConFiltros(Integer idEmpresa, LocalDate fInicio, LocalDate fFin, String cliente) {
        LocalDateTime inicio = (fInicio != null) ? fInicio.atStartOfDay() : null;
        LocalDateTime fin = (fFin != null) ? fFin.atTime(23, 59, 59) : null;
        String queryCliente = (cliente != null && !cliente.trim().isEmpty()) ? cliente.trim() : null;
        return ventaRepository.buscarConFiltros(idEmpresa, inicio, fin, queryCliente);
    }

    @Transactional(readOnly = true)
    public Venta obtenerVentaPorIdYEmpresa(Integer id, Integer idEmpresa) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
        if (!venta.getIdEmpresa().equals(idEmpresa)) {
            throw new RuntimeException("Acceso denegado.");
        }
        Hibernate.initialize(venta.getDetalles());
        venta.getDetalles().forEach(d -> Hibernate.initialize(d.getProducto()));
        return venta;
    }

    // --- LÓGICA DE PROCESAMIENTO DE VENTA ---

    @Transactional
    public Venta guardarVenta(VentaDTO ventaDto, Integer idUsuarioActual, Integer idEmpresa) {
        Integer idSesionActiva = obtenerSesionActiva(idEmpresa);
        Integer idClienteFinal = (ventaDto.getIdCliente() != null) ? ventaDto.getIdCliente() : 1;
        Integer idTipoDoc = (ventaDto.getIdTipoDoc() != null) ? ventaDto.getIdTipoDoc() : 1;

        Cliente clienteBD = clienteRepository.findById(idClienteFinal)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        CorrelativoDocumento corr = correlativoRepository.findActual(idTipoDoc, 1, idEmpresa)
                .orElseThrow(() -> new RuntimeException("No hay correlativo configurado."));

        int proximoNumero = corr.getNumeroActual() + 1;

        Venta venta = new Venta();
        venta.setIdEmpresa(idEmpresa);
        venta.setIdSucursal(ventaDto.getIdSucursal() != null ? ventaDto.getIdSucursal() : 1);
        venta.setIdUsuario(idUsuarioActual);
        venta.setIdSesion(idSesionActiva);
        venta.setIdTipoDoc(idTipoDoc);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstado("COMPLETADA");
        venta.setIdCorrelativo(corr.getIdCorrelativo());
        venta.setNumeroDocumento(proximoNumero);
        venta.setIdCliente(idClienteFinal);
        venta.setNombreCliente(idClienteFinal == 1 ?
                (ventaDto.getNombreTemporal() != null ? ventaDto.getNombreTemporal().toUpperCase() : "CONSUMIDOR FINAL") :
                clienteBD.getNombre().toUpperCase());

        BigDecimal totalAcumulado = BigDecimal.ZERO;

        for (VentaDTO.ItemDetalle item : ventaDto.getDetalles()) {
            Producto p = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + item.getIdProducto()));

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(p);
            detalle.setIdEmpresa(idEmpresa);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            detalle.setCostoHistorico(p.getCostoUltimo() != null ? p.getCostoUltimo() : 0.0);

            totalAcumulado = totalAcumulado.add(BigDecimal.valueOf(item.getPrecioUnitario())
                    .multiply(BigDecimal.valueOf(item.getCantidad())));

            venta.addDetalle(detalle);

            jdbcTemplate.update("UPDATE inventario SET stock_actual = stock_actual - ? WHERE id_producto = ? AND id_empresa = ?",
                    item.getCantidad(), item.getIdProducto(), idEmpresa);
        }

        venta.setTotal(totalAcumulado);
        corr.setNumeroActual(proximoNumero);
        correlativoRepository.save(corr);

        Venta ventaGuardada = ventaRepository.save(venta);
        procesarPagos(ventaDto, ventaGuardada, idSesionActiva, totalAcumulado, proximoNumero, idEmpresa);

        return ventaGuardada;
    }

    // --- ANULACIÓN CORREGIDA ---

    @Transactional
    public void anularVentaSegura(Integer idVenta, Integer idEmpresa) {
        // Obtenemos la venta con sus detalles cargados
        Venta venta = obtenerVentaPorIdYEmpresa(idVenta, idEmpresa);

        if ("ANULADA".equals(venta.getEstado())) {
            throw new RuntimeException("La venta ya se encuentra anulada.");
        }

        // 1. Restaurar el stock de cada producto
        for (DetalleVenta d : venta.getDetalles()) {
            jdbcTemplate.update(
                    "UPDATE inventario SET stock_actual = stock_actual + ? WHERE id_producto = ? AND id_empresa = ?",
                    d.getCantidad(), d.getProducto().getIdProducto(), idEmpresa
            );
        }

        // 2. Registrar el movimiento de salida (EGRESO) en caja para cuadre contable
        jdbcTemplate.update(
                "INSERT INTO caja_movimiento (id_sesion, tipo_movimiento, tipo_referencia, id_referencia, monto, descripcion, fecha_movimiento, id_empresa, metodo_pago, id_venta) " +
                        "VALUES (?, 'EGRESO', 'VENTA', ?, ?, ?, NOW(), ?, 'ANULACION', ?)",
                venta.getIdSesion(), idVenta, venta.getTotal(), "ANULACIÓN VENTA #" + venta.getNumeroDocumento(), idEmpresa, idVenta
        );

        // 3. Actualizar estado de la venta
        venta.setEstado("ANULADA");
        ventaRepository.save(venta);
    }

    // --- MÉTODOS DE SOPORTE ---

    private void procesarPagos(VentaDTO ventaDto, Venta ventaGuardada, Integer idSesion, BigDecimal totalVenta, int numDoc, Integer idEmpresa) {
        double sumaPagos = ventaDto.getPagos().stream().mapToDouble(VentaDTO.PagoDetalle::getMonto).sum();
        BigDecimal totalPagado = BigDecimal.valueOf(sumaPagos);
        BigDecimal vueltoTotal = totalPagado.subtract(totalVenta).max(BigDecimal.ZERO);

        for (VentaDTO.PagoDetalle pago : ventaDto.getPagos()) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO venta_pago (id_venta, id_forma_pago, monto, referencia, id_banco, id_empresa) VALUES (?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, ventaGuardada.getIdVenta());
                ps.setInt(2, pago.getIdFormaPago());
                ps.setDouble(3, pago.getMonto());
                ps.setString(4, pago.getReferencia());
                if (pago.getIdBanco() != null) ps.setInt(5, pago.getIdBanco()); else ps.setNull(5, java.sql.Types.INTEGER);
                ps.setInt(6, idEmpresa);
                return ps;
            }, keyHolder);

            Integer idVentaPagoGenerated = Objects.requireNonNull(keyHolder.getKey()).intValue();
            Map<String, Object> infoFp = obtenerInfoFormaPago(pago.getIdFormaPago());
            String categoriaFp = (String) infoFp.get("tipo_categoria");
            String nombreFp = (String) infoFp.get("nombre");

            BigDecimal montoCaja = BigDecimal.valueOf(pago.getMonto());
            if ("EFECTIVO".equals(categoriaFp)) {
                montoCaja = montoCaja.subtract(vueltoTotal);
            }

            if (montoCaja.compareTo(BigDecimal.ZERO) > 0) {
                registrarMovimientoCaja(idSesion, montoCaja.doubleValue(), "VENTA DOC #" + numDoc + " (" + nombreFp + ")",
                        idEmpresa, categoriaFp, ventaGuardada.getIdVenta(), idVentaPagoGenerated, pago.getIdFormaPago());
            }
        }
    }

    private void registrarMovimientoCaja(Integer idSesion, Double monto, String descripcion,
                                         Integer idEmpresa, String metodoPago, Integer idVenta,
                                         Integer idVentaPago, Integer idFormaPago) {
        jdbcTemplate.update(
                "INSERT INTO caja_movimiento (id_sesion, tipo_movimiento, tipo_referencia, id_referencia, monto, descripcion, fecha_movimiento, id_empresa, metodo_pago, id_venta, id_venta_pago, id_forma_pago) " +
                        "VALUES (?, 'INGRESO', 'VENTA', ?, ?, ?, NOW(), ?, ?, ?, ?, ?)",
                idSesion, idVenta, monto, descripcion, idEmpresa, metodoPago, idVenta, idVentaPago, idFormaPago
        );
    }

    private Integer obtenerSesionActiva(Integer idEmpresa) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id_sesion FROM caja_sesion WHERE id_empresa = ? AND estado = 'ABIERTA' ORDER BY id_sesion DESC LIMIT 1",
                    Integer.class, idEmpresa
            );
        } catch (Exception e) {
            throw new RuntimeException("No hay una sesión de caja abierta.");
        }
    }

    private Map<String, Object> obtenerInfoFormaPago(Integer id) {
        try {
            return jdbcTemplate.queryForMap("SELECT nombre, tipo_categoria FROM forma_pago WHERE id_forma_pago = ?", id);
        } catch (Exception e) {
            return Map.of("nombre", "OTRO", "tipo_categoria", "GENERAL");
        }
    }

    public List<Map<String, Object>> obtenerFavoritos(Integer idEmpresa) {
        String sql = "SELECT id_producto, nombre, precio FROM producto WHERE es_favorito = true AND activo = true AND id_empresa = ? LIMIT 12";
        return jdbcTemplate.queryForList(sql, idEmpresa);
    }

    public List<Map<String, Object>> obtenerMasVendidosSemana(Integer idEmpresa) {
        String sql = "SELECT p.id_producto, p.nombre, p.precio, COUNT(dv.id_detalle) as conteo " +
                "FROM venta_detalle dv JOIN producto p ON dv.id_producto = p.id_producto " +
                "JOIN venta v ON dv.id_venta = v.id_venta " +
                "WHERE v.fecha_venta >= DATE_SUB(NOW(), INTERVAL 7 DAY) AND v.estado = 'COMPLETADA' AND v.id_empresa = ? " +
                "GROUP BY p.id_producto, p.nombre, p.precio ORDER BY conteo DESC LIMIT 8";
        return jdbcTemplate.queryForList(sql, idEmpresa);
    }
}