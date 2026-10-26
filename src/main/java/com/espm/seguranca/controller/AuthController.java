package com.espm.seguranca.controller;

import com.espm.seguranca.dto.LoginRequest;
import com.espm.seguranca.dto.MensagemResponse;
import com.espm.seguranca.dto.TrocaSenhaRequest;
import com.espm.seguranca.service.LoginService;
import com.espm.seguranca.service.ResultadoLogin;
import com.espm.seguranca.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Autenticacao", description = "Endpoints publicos: login e troca de senha")
public class AuthController {

    private final LoginService loginService;
    private final UsuarioService usuarioService;

    public AuthController(LoginService loginService, UsuarioService usuarioService) {
        this.loginService = loginService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    @Operation(summary = "Efetua o login de um usuario",
            description = "Valida username e senha na tabela usuarios. Ao falhar mais de uma vez o usuario e bloqueado. "
                    + "Endpoint publico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario e senha validos",
                    content = @Content(schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuario e senha invalidos (ou usuario bloqueado)",
                    content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
    })
    public ResponseEntity<MensagemResponse> login(@RequestBody LoginRequest request) {
        ResultadoLogin resultado = loginService.autenticar(request.username(), request.senha());

        return switch (resultado) {
            case SUCESSO -> ResponseEntity.ok(new MensagemResponse("Login realizado com sucesso"));
            case BLOQUEADO -> naoAutorizado("Usuario bloqueado. Procure um administrador para desbloquea-lo");
            case INVALIDO_E_BLOQUEADO ->
                    naoAutorizado("Usuario ou senha invalidos. Usuario bloqueado por excesso de tentativas");
            case INVALIDO -> naoAutorizado("Usuario ou senha invalidos");
        };
    }

    @PutMapping("/trocarsenha/{userid}")
    @Operation(summary = "Troca a senha de um usuario",
            description = "Confere a senha atual na base de dados e grava a nova senha. Endpoint publico. "
                    + "Uma senha atual incorreta conta como falha de login.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nova senha invalida (vazia ou com mais de 10 caracteres)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Senha atual incorreta", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario bloqueado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content)
    })
    public ResponseEntity<MensagemResponse> trocarSenha(
            @Parameter(description = "Username do usuario", example = "usuario")
            @PathVariable("userid") String userid,
            @Valid @RequestBody TrocaSenhaRequest request) {
        usuarioService.trocarSenha(userid, request);
        return ResponseEntity.ok(new MensagemResponse("Senha alterada com sucesso"));
    }

    private ResponseEntity<MensagemResponse> naoAutorizado(String mensagem) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MensagemResponse(mensagem));
    }
}
