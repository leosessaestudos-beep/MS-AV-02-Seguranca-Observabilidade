package com.espm.seguranca.service;

public enum ResultadoLogin {

    /** Usuario e senha corretos e usuario nao bloqueado. */
    SUCESSO,

    /** Usuario inexistente ou senha incorreta. */
    INVALIDO,

    /** Senha incorreta e esta falha fez o usuario atingir o limite e ser bloqueado. */
    INVALIDO_E_BLOQUEADO,

    /** Usuario ja estava bloqueado antes desta tentativa. */
    BLOQUEADO
}
