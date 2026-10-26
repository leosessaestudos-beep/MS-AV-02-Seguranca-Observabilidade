package com.espm.seguranca.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Usuario cadastrado (a senha nunca e retornada)")
public record UsuarioResponse(
        String username,
        List<String> roles,
        boolean bloqueado,
        int tentativasFalhas) {
}
