package com.puntofacil.puntofacilbackend.controller;

import com.puntofacil.puntofacilbackend.entity.Usuario;
import com.puntofacil.puntofacilbackend.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Vista de Perfil: Muestra los datos del usuario que tiene la sesión activa.
     */
    @GetMapping("/perfil")
    public String verPerfil(Model model, Principal principal) {
        // principal.getName() obtiene el 'username' del usuario autenticado
        Usuario usuario = usuarioService.buscarPorUsername(principal.getName());
        model.addAttribute("usuario", usuario);
        return "usuarios/perfil";
    }

    /**
     * Vista principal: Tabla de empleados (Mantenimiento).
     */
    @GetMapping("/mantenimiento")
    public String mantenimiento(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/mantenimiento";
    }

    /**
     * Formulario para crear un nuevo usuario.
     */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        // Evitamos sobreescribir si ya viene un objeto del error en 'guardar'
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        return "usuarios/formulario";
    }

    /**
     * Formulario para editar un usuario existente.
     */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        try {
            model.addAttribute("usuario", usuarioService.buscarPorId(id));
            return "usuarios/formulario";
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/usuarios/mantenimiento";
        }
    }

    /**
     * Procesa el guardado.
     * Si ocurre un error, devuelve al usuario al formulario correcto (nuevo o edición).
     */
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Usuario usuario, RedirectAttributes flash) {
        try {
            usuarioService.guardar(usuario);
            flash.addFlashAttribute("success", "Usuario procesado exitosamente.");
            return "redirect:/usuarios/mantenimiento";
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo guardar: " + e.getMessage());
            flash.addFlashAttribute("usuario", usuario); // Mantenemos los datos ingresados

            // Decidimos a qué vista regresar según si el ID ya existe
            if (usuario.getIdUsuario() != null) {
                return "redirect:/usuarios/editar/" + usuario.getIdUsuario();
            }
            return "redirect:/usuarios/nuevo";
        }
    }
}