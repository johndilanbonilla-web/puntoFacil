package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.entity.Rol;
import com.puntofacil.puntofacilbackend.entity.Usuario;
import com.puntofacil.puntofacilbackend.repository.RolRepository;
import com.puntofacil.puntofacilbackend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository; // Nueva dependencia para el puente de roles
    private final PasswordEncoder passwordEncoder;

    // Constructor actualizado con la nueva dependencia
    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Guarda o actualiza un usuario aplicando lógica de cifrado inteligente
     * y sincronización de roles híbrida.
     */
    @Transactional
    public Usuario guardar(Usuario usuario) {

        // --- 1. SINCRONIZACIÓN DE ROL (EL PUENTE) ---
        // Extraemos el nombre del rol de la tabla maestra para llenar el String 'rol'
        if (usuario.getRoleRelacional() != null && usuario.getRoleRelacional().getIdRole() != null) {
            Rol rolDb = rolRepository.findById(usuario.getRoleRelacional().getIdRole())
                    .orElseThrow(() -> new RuntimeException("El Rol seleccionado no existe"));

            // Llenamos el campo String para no romper la seguridad ni consultas viejas
            usuario.setRol(rolDb.getNombreRol());
        }

        if (usuario.getIdUsuario() == null) {
            // --- 2. LÓGICA PARA NUEVO USUARIO ---
            validarUsernameUnico(usuario.getUsername());

            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                throw new RuntimeException("La contraseña es obligatoria para nuevos usuarios");
            }

            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

            // Por defecto, nuevos usuarios están activos
            if (usuario.getActivo() == null) usuario.setActivo(true);

        } else {
            // --- 3. LÓGICA PARA EDICIÓN ---
            Usuario usuarioExistente = usuarioRepository.findById(usuario.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuario.getIdUsuario()));

            // Validar si intentan cambiar el username a uno que ya existe
            if (!usuarioExistente.getUsername().equals(usuario.getUsername())) {
                validarUsernameUnico(usuario.getUsername());
            }

            // Gestión de contraseña en edición
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                usuario.setPassword(usuarioExistente.getPassword()); // Preservar actual cifrada
            } else {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword())); // Cifrar nueva
            }

            // Preservar idEmpresa original si no viene en el objeto
            if (usuario.getIdEmpresa() == null) {
                usuario.setIdEmpresa(usuarioExistente.getIdEmpresa());
            }

            // Preservar sucursal si no se cambia en el formulario
            if (usuario.getSucursal() == null) {
                usuario.setSucursal(usuarioExistente.getSucursal());
            }
        }

        return usuarioRepository.save(usuario);
    }

    private void validarUsernameUnico(String username) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("El nombre de usuario '" + username + "' ya está en uso.");
        }
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario '" + username + "' no encontrado"));
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }
}