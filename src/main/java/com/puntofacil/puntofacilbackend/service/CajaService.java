package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.entity.CajaSesion;
import com.puntofacil.puntofacilbackend.repository.CajaSesionRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CajaService {

    private final CajaSesionRepository cajaSesionRepository;

    public CajaService(CajaSesionRepository cajaSesionRepository) {
        this.cajaSesionRepository = cajaSesionRepository;
    }

    /**
     * Verifica si la empresa actual tiene una caja abierta.
     */
    public boolean tieneSesionActiva(Integer idEmpresa) {
        return cajaSesionRepository.existsByIdEmpresaAndEstado(idEmpresa, "ABIERTA");
    }

    /**
     * Obtiene la sesión activa filtrada por empresa.
     * Este es el método que usará el POS para registrar ventas.
     */
    public Optional<CajaSesion> obtenerSesionActiva(Integer idEmpresa) {
        // Buscamos estrictamente por empresa y estado
        return cajaSesionRepository.findByIdEmpresaAndEstado(idEmpresa, "ABIERTA");
    }

    /**
     * Búsqueda más específica por Empresa y Sucursal.
     * Útil si el negocio escala a tener varias sucursales físicas.
     */
    public Optional<CajaSesion> obtenerSesionPorSucursal(Integer idEmpresa, Integer idSucursal) {
        return cajaSesionRepository.findByIdEmpresaAndIdSucursalAndEstado(idEmpresa, idSucursal, "ABIERTA");
    }
}