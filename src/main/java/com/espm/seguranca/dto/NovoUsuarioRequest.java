package com.espm.seguranca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de um novo usuario")
public record NovoUsuarioRequest(
        @Schema(description = "Nome do usuario", example = "joao")
        @NotBlank(message = "O username e obrigatorio")
        @Size(max = 100, message = "O username deve ter no maximo 100 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._@-]+$",
                message = "O username deve conter apenas letras, numeros e os caracteres . _ @ -") String username,

        @Schema(description = "Senha (maximo 10 caracteres)", example = "senha123")
        @NotBlank(message = "A senha e obrigatoria")
        @Size(max = 10, message = "A senha deve ter no maximo 10 caracteres") String senha) {
}
