package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.entity.CajaSesion;
import com.puntofacil.puntofacilbackend.entity.CajaMovimiento;
import com.puntofacil.puntofacilbackend.repository.CajaSesionRepository;
import com.puntofacil.puntofacilbackend.repository.CajaMovimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class CajaService {

    private final CajaSesionRepository cajaSesionRepository;
    private final CajaMovimientoRepository cajaMovimientoRepository;

    public CajaService(CajaSesionRepository cajaSesionRepository,
                       CajaMovimientoRepository cajaMovimientoRepository) {
        this.cajaSesionRepository = cajaSesionRepository;
        this.cajaMovimientoRepository = cajaMovimientoRepository;
    }

    // ==========================================
    // 1. HISTORIAL Y CONSULTAS
    // ==========================================

    @Transactional(readOnly = true)
    public List<CajaSesion> obtenerHistorialGestion(Integer idEmpresa, Integer idSucursal, LocalDate inicio, LocalDate fin) {
        LocalDateTime start = inicio.atStartOfDay();
        LocalDateTime end = fin.atTime(LocalTime.MAX);

        List<CajaSesion> sesiones = cajaSesionRepository.buscarSesionesHistoricas(idEmpresa, idSucursal, start, end);

        sesiones.forEach(s -> {
            BigDecimal ventas = cajaSesionRepository.sumTotalVentasBySesion(s.getIdSesion());
            BigDecimal gastos = cajaSesionRepository.sumTotalGastosBySesion(s.getIdSesion());
            BigDecimal compras = cajaSesionRepository.sumTotalComprasBySesion(s.getIdSesion());

            s.setTotalVentas(ventas);
            s.setTotalGastos(gastos.add(compras));
        });

        return sesiones;
    }

    @Transactional(readOnly = true)
    public Optional<CajaSesion> obtenerSesionActiva(Integer idEmpresa, Integer idUsuario) {
        return cajaSesionRepository.findByEstadoAndIdUsuarioAndIdEmpresa("ABIERTA", idUsuario, idEmpresa);
    }

    // ==========================================
    // 2. LÓGICA DE APERTURA DE CAJA
    // ==========================================

    @Transactional
    public CajaSesion abrirCaja(CajaSesion nuevaSesion) {
        boolean tieneCaja = cajaSesionRepository
                .findByEstadoAndIdUsuarioAndIdEmpresa("ABIERTA", nuevaSesion.getIdUsuario(), nuevaSesion.getIdEmpresa())
                .isPresent();

        if (tieneCaja) {
            throw new IllegalStateException("El usuario ya tiene una sesión de caja abierta.");
        }

        nuevaSesion.setFechaApertura(LocalDateTime.now());
        nuevaSesion.setEstado("ABIERTA");
        CajaSesion sesionGuardada = cajaSesionRepository.save(nuevaSesion);

        registrarMovimientoApertura(sesionGuardada);

        return sesionGuardada;
    }

    private void registrarMovimientoApertura(CajaSesion sesion) {
        CajaMovimiento mov = new CajaMovimiento();
        mov.setIdEmpresa(sesion.getIdEmpresa());
        mov.setIdSesion(sesion.getIdSesion());
        mov.setTipoReferencia("APERTURA");
        mov.setTipoMovimiento("INGRESO");

        // CORRECCIÓN 1: getMontoApertura() ya es BigDecimal, se asigna directo
        mov.setMonto(sesion.getMontoApertura());

        mov.setIdFormaPago(1); // 1 = EFECTIVO
        mov.setDescripcion("Apertura de caja inicial");
        cajaMovimientoRepository.save(mov);
    }

    // ==========================================
    // 3. LÓGICA DE CIERRE DE CAJA Y ARQUEO
    // ==========================================

    @Transactional
    public CajaSesion cerrarCaja(Integer idSesion, BigDecimal montoCierreReal) {
        CajaSesion sesion = cajaSesionRepository.findById(idSesion)
                .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada."));

        if ("CERRADA".equals(sesion.getEstado())) {
            throw new IllegalStateException("La caja ya se encuentra cerrada.");
        }

        BigDecimal efectivoEsperado = calcularEfectivoEsperado(sesion);

        sesion.setFechaCierre(LocalDateTime.now());
        sesion.setEstado("CERRADA");

        // CORRECCIÓN 2: Se asignan los BigDecimal directamente sin .doubleValue()
        sesion.setMontoCierreSistema(efectivoEsperado);
        sesion.setMontoCierreReal(montoCierreReal);

        return cajaSesionRepository.save(sesion);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularEfectivoEsperado(CajaSesion sesion) {
        BigDecimal ingresosEfectivo = cajaMovimientoRepository.sumVentasEfectivoBySesion(sesion.getIdSesion());

        // Aquí deberías sumar los egresos en efectivo si tienes ese método en tu repo
        BigDecimal egresosEfectivo = BigDecimal.ZERO;

        // CORRECCIÓN 3: Se usa directamente el BigDecimal de la entidad
        BigDecimal apertura = sesion.getMontoApertura();

        return apertura.add(ingresosEfectivo).subtract(egresosEfectivo);
    }
}