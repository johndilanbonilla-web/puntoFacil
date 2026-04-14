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

        // 1. Solo actuar si hay un modelo para la vista (páginas HTML)
        if (modelAndView != null && !modelAndView.getViewName().startsWith("redirect:")) {

            HttpSession session = request.getSession();
            Integer idEmpresa = (Integer) session.getAttribute("id_empresa");

            // Si el atributo id_empresa tiene otro nombre en tu sesión, búscalo también así:
            if (idEmpresa == null) idEmpresa = (Integer) session.getAttribute("idEmpresa");

            // 2. Si hay una empresa en sesión, verificamos SU caja
            if (idEmpresa != null) {
                // Usamos el nuevo método del Service que creamos antes
                boolean estaAbierta = cajaService.tieneSesionActiva(idEmpresa);

                // Inyecta la variable que usas en Thymeleaf (ej: th:if="${cajaActiva}")
                modelAndView.addObject("cajaActiva", estaAbierta);
            } else {
                // Si no hay sesión, por seguridad enviamos false
                modelAndView.addObject("cajaActiva", false);
            }
        }
    }
}