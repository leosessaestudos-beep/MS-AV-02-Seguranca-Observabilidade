package com.espm.seguranca.repository;

import com.espm.seguranca.dto.UsuarioResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acesso a tabela "usuarios" (credenciais) e consultas auxiliares.
 */
@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existe(String username) {
        Long total = jdbc.queryForObject("select count(*) from usuarios where username = ?", Long.class, username);
        return total != null && total > 0;
    }

    /** Retorna a senha cadastrada ou null (usuario inexistente ou sem senha). */
    public String buscarSenha(String username) {
        List<String> resultado = jdbc.queryForList("select senha from usuarios where username = ?", String.class, username);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public void atualizarSenha(String username, String novaSenha) {
        jdbc.update("update usuarios set senha = ? where username = ?", novaSenha, username);
    }

    /** Lista todos os usuarios com suas roles e situacao de bloqueio (sem expor a senha). */
    public List<UsuarioResponse> listar() {
        Map<String, List<String>> rolesPorUsuario = new HashMap<>();
        List<Map<String, Object>> linhasRoles = jdbc.queryForList(
                "select username, authority from authorities order by authority");
        for (Map<String, Object> linha : linhasRoles) {
            rolesPorUsuario
                    .computeIfAbsent((String) linha.get("username"), chave -> new ArrayList<>())
                    .add((String) linha.get("authority"));
        }

        List<Map<String, Object>> linhasUsuarios = jdbc.queryForList(
                "select u.username, coalesce(l.locked, false) as locked, coalesce(l.failed_attempts, 0) as failed_attempts "
                        + "from usuarios u left join login_attempts l on l.username = u.username "
                        + "order by u.username");

        List<UsuarioResponse> usuarios = new ArrayList<>();
        for (Map<String, Object> linha : linhasUsuarios) {
            String username = (String) linha.get("username");
            boolean bloqueado = Boolean.TRUE.equals(linha.get("locked"));
            int tentativas = ((Number) linha.get("failed_attempts")).intValue();
            usuarios.add(new UsuarioResponse(
                    username,
                    rolesPorUsuario.getOrDefault(username, List.of()),
                    bloqueado,
                    tentativas));
        }
        return usuarios;
    }
}
