package com.espm.seguranca.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class SenhaUtil {

    private SenhaUtil() {
    }

    /** Compara duas senhas em tempo constante (evita ataques de timing). Nulos nunca sao iguais. */
    public static boolean iguais(String esperada, String informada) {
        if (esperada == null || informada == null) {
            return false;
        }
        return MessageDigest.isEqual(
                esperada.getBytes(StandardCharsets.UTF_8),
                informada.getBytes(StandardCharsets.UTF_8));
    }
}
