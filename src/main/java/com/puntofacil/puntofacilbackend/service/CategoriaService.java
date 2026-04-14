package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.entity.Categoria;
import com.puntofacil.puntofacilbackend.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoriaService {
    @Autowired
    private CategoriaRepository repo;

    public List<Categoria> findAllByEmpresa(Integer idEmpresa) {
        return repo.findByIdEmpresa(idEmpresa);
    }
}