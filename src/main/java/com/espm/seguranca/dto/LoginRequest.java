package com.espm.seguranca.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciais para login")
public record LoginRequest(
        @Schema(description = "Nome do usuario", example = "usuario") String username,
        @Schema(description = "Senha do usuario", example = "user123") String senha) {
}
