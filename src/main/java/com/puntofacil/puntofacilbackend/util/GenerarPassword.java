package com.puntofacil.puntofacilbackend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerarPassword {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 👇 contraseña en texto plano
        String passwordPlano = "adminbs";

        // 👇 contraseña encriptada
        String passwordEncriptado = encoder.encode(passwordPlano);

        System.out.println("Password plano: " + passwordPlano);
        System.out.println("Password encriptado:");
        System.out.println(passwordEncriptado);
    }
}