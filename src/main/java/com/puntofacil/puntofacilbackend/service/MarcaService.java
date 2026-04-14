package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.entity.Marca;
import com.puntofacil.puntofacilbackend.repository.MarcaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MarcaService {
    @Autowired
    private MarcaRepository marcaRepository;

    public List<Marca> findAllByEmpresa(Integer idEmpresa) {
        return marcaRepository.findByIdEmpresaAndActivoTrue(idEmpresa);
    }
}