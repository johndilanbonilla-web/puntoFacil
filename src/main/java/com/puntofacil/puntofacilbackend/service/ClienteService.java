package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.entity.Cliente;
import com.puntofacil.puntofacilbackend.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Lista todos los clientes activos filtrados por la empresa actual.
     */
    public List<Cliente> listarActivos(Integer idEmpresa) {
        return clienteRepository.findByIdEmpresaAndActivoTrue(idEmpresa);
    }

    /**
     * Búsqueda inteligente por nombre, NIT o DUI.
     */
    public List<Cliente> buscarClientes(Integer idEmpresa, String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return listarActivos(idEmpresa);
        }
        return clienteRepository.buscarPorTermino(idEmpresa, termino.trim());
    }

    /**
     * GUARDA O ACTUALIZA:
     * Asegura que el cliente pertenezca a la empresa que realiza la acción.
     */
    @Transactional
    public Cliente guardar(Cliente cliente, Integer idEmpresaSesion) {
        // Regla de Oro Multi-SaaS: Forzar el ID de la empresa de la sesión
        cliente.setIdEmpresa(idEmpresaSesion);

        // Si es nuevo, asegurar que inicie activo
        if (cliente.getIdCliente() == null) {
            cliente.setActivo(true);
        }

        return clienteRepository.save(cliente);
    }

    /**
     * OBTENCIÓN SEGURA:
     * Valida que el ID solicitado realmente pertenezca a la empresa logueada.
     */
    public Optional<Cliente> obtenerPorIdYEmpresa(Integer id, Integer idEmpresa) {
        return clienteRepository.findByIdClienteAndIdEmpresa(id, idEmpresa);
    }

    /**
     * CONSUMIDOR FINAL AUTOMÁTICO:
     * Busca el cliente genérico. Si no existe para esta empresa, lo crea.
     */
    @Transactional
    public Cliente obtenerGenerico(Integer idEmpresa) {
        // Supongamos que en el Repository tienes findByNombreAndIdEmpresa
        return clienteRepository.findByNombreAndIdEmpresa("CONSUMIDOR FINAL", idEmpresa)
                .orElseGet(() -> {
                    Cliente c = new Cliente();
                    c.setNombre("CONSUMIDOR FINAL");
                    c.setNombreComercial("VENTA GENERAL");
                    c.setIdEmpresa(idEmpresa);
                    c.setDireccion("CIUDAD");
                    c.setIdTipoCliente(1); // 1 = Consumidor Final
                    c.setActivo(true);     // Cambiado de 1 a true por el tipo Boolean
                    return clienteRepository.save(c);
                });
    }

    /**
     * ANULACIÓN LÓGICA:
     * Nunca borramos de la BD, solo desactivamos.
     */
    @Transactional
    public boolean desactivarCliente(Integer id, Integer idEmpresa) {
        return obtenerPorIdYEmpresa(id, idEmpresa).map(c -> {
            c.setActivo(false);
            clienteRepository.save(c);
            return true;
        }).orElse(false);
    }
}