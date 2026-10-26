package com.espm.seguranca.controller;

import com.espm.seguranca.dto.MensagemResponse;
import com.espm.seguranca.dto.NovoUsuarioRequest;
import com.espm.seguranca.dto.UsuarioResponse;
import com.espm.seguranca.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Usuarios", description = "Manutencao de usuarios (exige autenticacao e role especifica)")
@SecurityRequirement(name = "basicAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/")
    @Operation(summary = "Cadastra um novo usuario",
            description = "Somente usuarios com a role ROLE_MANUTENCAO. O novo usuario recebe a role ROLE_USER.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario cadastrado",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content),
            @ApiResponse(responseCode = "409", description = "Username ja existe", content = @Content)
    })
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody NovoUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @GetMapping("/")
    @Operation(summary = "Lista todos os usuarios cadastrados",
            description = "Somente usuarios com a role ROLE_MANUTENCAO. A senha nunca e retornada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content)
    })
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PutMapping("/unlock/{userid}")
    @Operation(summary = "Desbloqueia um usuario",
            description = "Somente usuarios com a role ROLE_ADMIN. Zera o contador de falhas do usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario desbloqueado",
                    content = @Content(schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content)
    })
    public ResponseEntity<MensagemResponse> desbloquear(
            @Parameter(description = "Username do usuario a ser desbloqueado", example = "aluno1")
            @PathVariable("userid") String userid) {
        usuarioService.desbloquear(userid);
        return ResponseEntity.ok(new MensagemResponse("Usuario " + userid + " desbloqueado com sucesso"));
    }
}
