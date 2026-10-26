package com.espm.seguranca.exception;

public class UsuarioBloqueadoException extends RuntimeException {

    public UsuarioBloqueadoException(String username) {
        super("Usuario bloqueado: " + username + ". Procure um administrador para desbloquea-lo");
    }
}
