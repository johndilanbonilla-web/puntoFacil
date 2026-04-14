package com.puntofacil.puntofacilbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CajaInterceptor cajaInterceptor;

    public WebConfig(CajaInterceptor cajaInterceptor) {
        this.cajaInterceptor = cajaInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Se aplica a todas las rutas (/**) para que la barra siempre esté actualizada
        registry.addInterceptor(cajaInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**"); // No procesar archivos estáticos
    }
}