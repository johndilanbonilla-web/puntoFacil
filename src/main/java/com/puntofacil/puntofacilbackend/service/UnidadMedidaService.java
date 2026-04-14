package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.entity.UnidadMedida;
import com.puntofacil.puntofacilbackend.repository.UnidadMedidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UnidadMedidaService {
    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    public List<UnidadMedida> findAllByEmpresa(Integer idEmpresa) {
        return unidadMedidaRepository.findByIdEmpresaAndActivoTrue(idEmpresa);
    }
}