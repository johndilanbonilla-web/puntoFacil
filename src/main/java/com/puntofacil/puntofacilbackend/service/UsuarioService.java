package com.puntofacil.puntofacilbackend.service;

import com.puntofacil.puntofacilbackend.entity.Usuario;
import com.puntofacil.puntofacilbackend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Guarda o actualiza un usuario aplicando lógica de cifrado inteligente.
     */
    @Transactional
    public Usuario guardar(Usuario usuario) {
        if (usuario.getIdUsuario() == null) {
            // --- LÓGICA PARA NUEVO USUARIO ---
            validarUsernameUnico(usuario.getUsername());

            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                throw new RuntimeException("La contraseña es obligatoria para nuevos usuarios");
            }

            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            // Por defecto, nuevos usuarios están activos
            if (usuario.getActivo() == null) usuario.setActivo(true);

        } else {
            // --- LÓGICA PARA EDICIÓN ---
            Usuario usuarioExistente = usuarioRepository.findById(usuario.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuario.getIdUsuario()));

            // Validar si intentan cambiar el username a uno que ya existe (y no es el suyo)
            if (!usuarioExistente.getUsername().equals(usuario.getUsername())) {
                validarUsernameUnico(usuario.getUsername());
            }

            // Si el password enviado está vacío, preservamos el actual ya cifrado
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                usuario.setPassword(usuarioExistente.getPassword());
            } else {
                // Si enviaron algo, lo ciframos (cambio de clave)
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }

            // Mantener el idEmpresa original si no se envió en el objeto
            if (usuario.getIdEmpresa() == null) {
                usuario.setIdEmpresa(usuarioExistente.getIdEmpresa());
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