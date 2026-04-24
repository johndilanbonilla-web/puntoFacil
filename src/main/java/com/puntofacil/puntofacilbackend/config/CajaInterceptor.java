package com.puntofacil.puntofacilbackend.config;

import com.puntofacil.puntofacilbackend.service.CajaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class CajaInterceptor implements HandlerInterceptor {

    private final CajaService cajaService;

    public CajaInterceptor(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        // 1. Solo actuar si hay un modelo y no es una redirección
        if (modelAndView != null && !modelAndView.getViewName().startsWith("redirect:")) {

            HttpSession session = request.getSession();

            // Unificamos la búsqueda de atributos (usando el estándar camelCase que vimos en tu Controller)
            Integer idEmpresa = (Integer) session.getAttribute("idEmpresa");
            Integer idUsuario = (Integer) session.getAttribute("idUsuario");

            // 2. Seguridad Multiusuario: Verificamos si ESTE usuario tiene caja abierta en ESTA empresa
            if (idEmpresa != null && idUsuario != null) {
                // Usamos el método que ya refactorizamos en el Service
                boolean estaAbierta = cajaService.obtenerSesionActiva(idEmpresa, idUsuario).isPresent();

                // Inyectamos la variable global para Thymeleaf
                modelAndView.addObject("cajaActiva", estaAbierta);
            } else {
                modelAndView.addObject("cajaActiva", false);
            }
        }
    }
}