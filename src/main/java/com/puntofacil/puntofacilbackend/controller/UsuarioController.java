package com.puntofacil.puntofacilbackend.controller;

import com.puntofacil.puntofacilbackend.entity.Usuario;
import com.puntofacil.puntofacilbackend.service.UsuarioService;
import com.puntofacil.puntofacilbackend.repository.RolRepository;
import com.puntofacil.puntofacilbackend.repository.SucursalRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;       // Inyectado para el combo box
    private final SucursalRepository sucursalRepository; // Inyectado para el combo box

    public UsuarioController(UsuarioService usuarioService,
                             RolRepository rolRepository,
                             SucursalRepository sucursalRepository) {
        this.usuarioService = usuarioService;
        this.rolRepository = rolRepository;
        this.sucursalRepository = sucursalRepository;
    }

    /**
     * Vista de Perfil
     */
    @GetMapping("/perfil")
    public String verPerfil(Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorUsername(principal.getName());
        model.addAttribute("usuario", usuario);
        return "usuarios/perfil";
    }

    /**
     * Vista principal (Mantenimiento)
     */
    @GetMapping("/mantenimiento")
    public String mantenimiento(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/mantenimiento";
    }

    /**
     * Formulario para nuevo usuario
     */
    @GetMapping("/nuevo")
    public String nuevo(Model model, Principal principal) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }

        // CARGA DE LISTAS MAESTRAS
        cargarListasMaestras(model, principal);

        return "usuarios/formulario";
    }

    /**
     * Formulario para editar usuario
     */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash, Principal principal) {
        try {
            model.addAttribute("usuario", usuarioService.buscarPorId(id));

            // CARGA DE LISTAS MAESTRAS
            cargarListasMaestras(model, principal);

            return "usuarios/formulario";
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/usuarios/mantenimiento";
        }
    }

    /**
     * Método privado para no repetir código en 'nuevo' y 'editar'
     */
    private void cargarListasMaestras(Model model, Principal principal) {
        // Obtenemos los datos del administrador logueado para filtrar sucursales por su empresa
        Usuario adminLogueado = usuarioService.buscarPorUsername(principal.getName());

        model.addAttribute("roles", rolRepository.findAll());
        // Filtramos sucursales para que solo vea las de su propia empresa (ID 1, 2, etc.)
        model.addAttribute("sucursales", sucursalRepository.findByIdEmpresaAndActivo(adminLogueado.getIdEmpresa(), 1));
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Usuario usuario, RedirectAttributes flash) {
        try {
            usuarioService.guardar(usuario);
            flash.addFlashAttribute("success", "Usuario procesado exitosamente.");
            return "redirect:/usuarios/mantenimiento";
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "Error: " + e.getMessage());
            flash.addFlashAttribute("usuario", usuario);
            return (usuario.getIdUsuario() != null)
                    ? "redirect:/usuarios/editar/" + usuario.getIdUsuario()
                    : "redirect:/usuarios/nuevo";
        }
    }
}