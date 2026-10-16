package com.umc.pediupatrao.entity;

public enum Perfil {
    ADMIN,
    GERENTE,
    ATENDENTE;

    public static boolean isValido(String valor) {
        if (valor == null) {
            return false;
        }
        for (Perfil perfil : values()) {
            if (perfil.name().equals(valor)) {
                return true;
            }
        }
        return false;
    }
}
