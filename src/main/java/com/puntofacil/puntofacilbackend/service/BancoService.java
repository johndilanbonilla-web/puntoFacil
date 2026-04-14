package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.entity.Banco;
import com.puntofacil.puntofacilbackend.repository.BancoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BancoService {

    @Autowired
    private BancoRepository bancoRepository;

    /**
     * Lista los bancos activos filtrados por la empresa del usuario.
     * @param idEmpresa ID de la empresa en sesión.
     */
    public List<Banco> listarBancosPorEmpresa(Integer idEmpresa) {
        // Pasamos 1 como el valor de 'activo'
        return bancoRepository.findByIdEmpresaAndActivoOrderByNombreBancoAsc(idEmpresa, 1);
    }
}