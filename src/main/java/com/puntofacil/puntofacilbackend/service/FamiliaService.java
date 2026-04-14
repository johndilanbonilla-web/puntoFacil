package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.entity.Familia;
import com.puntofacil.puntofacilbackend.repository.FamiliaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FamiliaService {
    @Autowired
    private FamiliaRepository familiaRepository;

    public List<Familia> findAllByEmpresa(Integer idEmpresa) {
        return familiaRepository.findByIdEmpresaAndActivoTrue(idEmpresa);
    }
}