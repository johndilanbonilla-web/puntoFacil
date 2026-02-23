package com.puntofacil.puntofacilbackend.service.security;

import com.puntofacil.puntofacilbackend.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UsuarioPrincipal implements UserDetails {

    private final Usuario usuario;

    public UsuarioPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

    // =========================
    // DATOS DE AUTENTICACIÓN
    // =========================

    @Override
    public String getUsername() {
        return usuario.getUsuario(); // ✅ CORRECTO
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    // =========================
    // ROLES / AUTORIDADES
    // =========================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol())
        );
    }

    // =========================
    // ESTADOS DE LA CUENTA
    // =========================

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.getActivo(); // opcional pero recomendado
    }

    // =========================
    // ACCESO AL USUARIO REAL
    // =========================

    public Usuario getUsuario() {
        return usuario;
    }
}