package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.dto.GastoDTO;
import com.puntofacil.puntofacilbackend.entity.Gasto;
import com.puntofacil.puntofacilbackend.repository.GastoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GastoService {

    @Autowired private GastoRepository gastoRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    /**
     * Obtiene el historial filtrado por rango de fechas.
     */
    public List<Map<String, Object>> obtenerGastosPorEmpresaYRango(Integer idEmpresa, String desde, String hasta) {
        StringBuilder sql = new StringBuilder(
                "SELECT g.id_gasto, g.monto, g.descripcion_personalizada as descripcion, " +
                        "g.fecha_gasto as fecha, p.nombre as nombreProducto, fp.nombre as nombreFormaPago, " +
                        "b.nombre_banco as nombreBanco, g.estado " +
                        "FROM gasto g " +
                        "JOIN producto p ON g.id_producto_gasto = p.id_producto " +
                        "JOIN forma_pago fp ON g.id_forma_pago = fp.id_forma_pago " +
                        "LEFT JOIN banco b ON g.id_banco = b.id_banco " +
                        "WHERE g.id_empresa = ? "
        );

        List<Object> params = new ArrayList<>();
        params.add(idEmpresa);

        if (desde != null && !desde.isEmpty() && hasta != null && !hasta.isEmpty()) {
            sql.append("AND g.fecha_gasto BETWEEN ? AND ? ");
            params.add(desde + " 00:00:00");
            params.add(hasta + " 23:59:59");
        } else {
            sql.append("AND g.fecha_gasto >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) ");
        }

        sql.append("ORDER BY g.fecha_gasto DESC");

        List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        for (Map<String, Object> row : resultados) {
            row.put("fecha", convertirALocalDateTime(row.get("fecha")));
            row.put("activo", "ACTIVO".equals(row.get("estado")));
        }
        return resultados;
    }

    /**
     * Recupera un gasto individual adaptado para el comprobante visual.
     * Incluye la dirección de la empresa recientemente agregada.
     */
    public Map<String, Object> obtenerGastoPorId(Integer id) {
        String sql = "SELECT g.*, " +
                "g.descripcion_personalizada as descripcion, " +
                "p.nombre as nombreProducto, " +
                "fp.nombre as nombreFormaPago, " +
                "b.nombre_banco as nombreBanco, " +
                "u1.username as usuarioCreador, " +
                "e.nombre as nombreEmpresa, " +
                "e.nit_rut as nitEmpresa, " +
                "e.direccion as direccionEmpresa, " + // <-- Nueva columna mapeada
                "e.telefono as telefonoEmpresa " +
                "FROM gasto g " +
                "JOIN producto p ON g.id_producto_gasto = p.id_producto " +
                "JOIN forma_pago fp ON g.id_forma_pago = fp.id_forma_pago " +
                "JOIN empresa e ON g.id_empresa = e.id_empresa " +
                "LEFT JOIN banco b ON g.id_banco = b.id_banco " +
                "LEFT JOIN usuario u1 ON g.id_usuario = u1.id_usuario " +
                "WHERE g.id_gasto = ?";
        try {
            Map<String, Object> resultado = jdbcTemplate.queryForMap(sql, id);

            LocalDateTime fechaGasto = convertirALocalDateTime(resultado.get("fecha_gasto"));
            resultado.put("fecha_gasto", fechaGasto);
            resultado.put("fecha", fechaGasto);
            resultado.put("fecha_anulacion", convertirALocalDateTime(resultado.get("fecha_anulacion")));
            resultado.put("activo", "ACTIVO".equals(resultado.get("estado")));

            return resultado;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Transactional
    public void anularGasto(Integer idGasto, Integer idUsuarioAnula) {
        Map<String, Object> gasto;
        try {
            gasto = jdbcTemplate.queryForMap(
                    "SELECT monto, id_empresa, descripcion_personalizada, id_forma_pago, estado " +
                            "FROM gasto WHERE id_gasto = ?", idGasto);
        } catch (Exception e) {
            throw new RuntimeException("El gasto solicitado no existe.");
        }

        if ("ANULADO".equals(gasto.get("estado"))) {
            throw new RuntimeException("Este gasto ya se encuentra anulado.");
        }

        jdbcTemplate.update(
                "UPDATE gasto SET estado = 'ANULADO', id_usuario_anula = ?, fecha_anulacion = NOW() WHERE id_gasto = ?",
                idUsuarioAnula, idGasto
        );

        Double monto = ((Number) gasto.get("monto")).doubleValue();
        Integer idEmpresa = (Integer) gasto.get("id_empresa");
        Integer idSesionActiva = obtenerSesionActiva(idEmpresa);
        Integer idFp = (Integer) gasto.get("id_forma_pago");
        Map<String, Object> infoFp = obtenerInfoFormaPago(idFp);

        String sqlMov = "INSERT INTO caja_movimiento (id_sesion, tipo_movimiento, tipo_referencia, id_referencia, " +
                "monto, descripcion, fecha_movimiento, id_empresa, metodo_pago, id_forma_pago) " +
                "VALUES (?, 'ENTRADA', 'GASTO', ?, ?, ?, NOW(), ?, ?, ?)";

        jdbcTemplate.update(sqlMov, idSesionActiva, idGasto, monto,
                "ANULACIÓN GASTO #" + idGasto + ": " + gasto.get("descripcion_personalizada"),
                idEmpresa, infoFp.get("tipo_categoria"), idFp);
    }

    @Transactional
    public Gasto registrarGasto(GastoDTO dto, Integer idUsuario, Integer idEmpresa) {
        Integer idSesionActiva = obtenerSesionActiva(idEmpresa);

        Gasto gasto = new Gasto();
        gasto.setIdEmpresa(idEmpresa);
        gasto.setIdProductoGasto(dto.getIdProducto());
        gasto.setMonto(dto.getMonto());

        String desc = (dto.getDescripcion() != null && !dto.getDescripcion().isBlank())
                ? dto.getDescripcion().toUpperCase()
                : "GASTO GENERAL";

        gasto.setDescripcionPersonalizada(desc);
        gasto.setIdFormaPago(dto.getIdFormaPago());
        gasto.setIdBanco(dto.getIdBanco());
        gasto.setIdUsuario(idUsuario);
        gasto.setFechaGasto(LocalDateTime.now());
        gasto.setEstado("ACTIVO");

        Gasto guardado = gastoRepository.save(gasto);

        Map<String, Object> infoFp = obtenerInfoFormaPago(dto.getIdFormaPago());
        registrarMovimientoCaja(idSesionActiva, "EGRESO", guardado.getIdGasto(),
                dto.getMonto(), desc, idEmpresa, infoFp, dto.getIdFormaPago());

        return guardado;
    }

    private LocalDateTime convertirALocalDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Timestamp ts) return ts.toLocalDateTime();
        if (obj instanceof LocalDateTime ldt) return ldt;
        return null;
    }

    private void registrarMovimientoCaja(Integer idSesion, String tipo, Integer idRef, Double monto,
                                         String desc, Integer idEmp, Map<String, Object> infoFp, Integer idFp) {
        String sql = "INSERT INTO caja_movimiento (id_sesion, tipo_movimiento, tipo_referencia, id_referencia, " +
                "monto, descripcion, fecha_movimiento, id_empresa, metodo_pago, id_forma_pago) " +
                "VALUES (?, ?, 'GASTO', ?, ?, ?, NOW(), ?, ?, ?)";

        jdbcTemplate.update(sql, idSesion, tipo, idRef, monto,
                tipo + ": " + desc, idEmp, infoFp.get("tipo_categoria"), idFp);
    }

    private Integer obtenerSesionActiva(Integer idEmpresa) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id_sesion FROM caja_sesion WHERE id_empresa = ? AND estado = 'ABIERTA' ORDER BY id_sesion DESC LIMIT 1",
                    Integer.class, idEmpresa);
        } catch (Exception e) {
            throw new RuntimeException("No hay una sesión de caja abierta.");
        }
    }

    private Map<String, Object> obtenerInfoFormaPago(Integer id) {
        try {
            return jdbcTemplate.queryForMap("SELECT nombre, tipo_categoria FROM forma_pago WHERE id_forma_pago = ?", id);
        } catch (Exception e) {
            return Map.of("nombre", "EFECTIVO", "tipo_categoria", "EFECTIVO");
        }
    }
}