import com.puntofacil.puntofacilbackend.entity.Empresa;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public List<Empresa> listar() {
        return empresaService.listar();
    }

    @PostMapping
    public Empresa crear(@RequestBody Empresa empresa) {
        return empresaService.guardar(empresa);
    }
}
