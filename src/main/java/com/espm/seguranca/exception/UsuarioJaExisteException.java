package com.espm.seguranca.exception;

public class UsuarioJaExisteException extends RuntimeException {

    public UsuarioJaExisteException(String username) {
        super("Ja existe um usuario com o username: " + username);
    }
}
