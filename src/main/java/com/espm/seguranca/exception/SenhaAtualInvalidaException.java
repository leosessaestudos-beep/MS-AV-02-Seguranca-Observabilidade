package com.espm.seguranca.exception;

public class SenhaAtualInvalidaException extends RuntimeException {

    public SenhaAtualInvalidaException() {
        super("Senha atual invalida");
    }
}
