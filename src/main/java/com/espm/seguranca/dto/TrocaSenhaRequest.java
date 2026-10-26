package com.espm.seguranca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para troca de senha")
public record TrocaSenhaRequest(
        @Schema(description = "Senha atual", example = "user123") String atual,

        @Schema(description = "Nova senha (maximo 10 caracteres)", example = "nova1234")
        @NotBlank(message = "A nova senha e obrigatoria")
        @Size(max = 10, message = "A nova senha deve ter no maximo 10 caracteres") String nova) {
}
