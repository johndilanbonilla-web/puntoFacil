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
        registry.addInterceptor(cajaInterceptor)
                .addPathPatterns("/**")
                // Añadimos /login, /logout y /api (si tienes) para que no ejecute consultas a BD innecesarias
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/login", "/logout", "/api/**");
    }
}