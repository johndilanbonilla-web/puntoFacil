package com.puntofacil.puntofacilbackend.config;

import com.puntofacil.puntofacilbackend.entity.Usuario;
import com.puntofacil.puntofacilbackend.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;

    public CustomLoginSuccessHandler(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();

        // Buscamos los datos reales del usuario en la DB
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error crítico: Usuario no encontrado tras autenticación."));

        HttpSession session = request.getSession();

        // --- CORRECCIÓN DE LLAVES (Debe coincidir con ViewController.java) ---

        // Antes tenías "id_usuario", el Controller busca "idUsuario"
        session.setAttribute("idUsuario", usuario.getIdUsuario().intValue());

        // Antes tenías "id_empresa", el Controller busca "idEmpresa"
        session.setAttribute("idEmpresa", usuario.getIdEmpresa().intValue());

        // Opcionales para la vista (Thymeleaf)
        session.setAttribute("nombreCompleto", usuario.getNombreCompleto());
        session.setAttribute("username", username);

        System.out.println("DEBUG: Sesión configurada para " + username + " (Empresa: " + usuario.getIdEmpresa() + ")");

        // Redirigimos al dashboard
        response.sendRedirect("/dashboard");
    }
}