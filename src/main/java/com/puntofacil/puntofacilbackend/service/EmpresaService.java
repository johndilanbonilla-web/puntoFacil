import com.puntofacil.puntofacilbackend.entity.Empresa;
import com.puntofacil.puntofacilbackend.repository.EmpresaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public List<Empresa> listar() {
        return empresaRepository.findAll();
    }

    public Empresa guardar(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    public Empresa buscarPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
    }
}
