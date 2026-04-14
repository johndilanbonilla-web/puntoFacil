package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.entity.FormaPago;
import com.puntofacil.puntofacilbackend.repository.FormaPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FormaPagoService {

    @Autowired
    private FormaPagoRepository repository;

    public List<FormaPago> listarActivas() {
        return repository.findByActivoOrderByNombreAsc(1);
    }
}