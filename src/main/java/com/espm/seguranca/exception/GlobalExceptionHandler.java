package com.espm.seguranca.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UsuarioJaExisteException.class)
    public ResponseEntity<Map<String, Object>> handleJaExiste(UsuarioJaExisteException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, Object>> handleChaveDuplicada(DuplicateKeyException ex) {
        return resposta(HttpStatus.CONFLICT, "Ja existe um usuario com este username");
    }

    @ExceptionHandler(UsuarioBloqueadoException.class)
    public ResponseEntity<Map<String, Object>> handleBloqueado(UsuarioBloqueadoException ex) {
        return resposta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(SenhaAtualInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleSenhaInvalida(SenhaAtualInvalidaException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> erros.put(erro.getField(), erro.getDefaultMessage()));

        Map<String, Object> body = corpo(HttpStatus.BAD_REQUEST, "Erro de validacao");
        body.put("erros", erros);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonInvalido(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisicao ausente ou JSON invalido");
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(corpo(status, mensagem));
    }

    private Map<String, Object> corpo(HttpStatus status, String mensagem) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("erro", status.getReasonPhrase());
        body.put("mensagem", mensagem);
        return body;
    }
}
