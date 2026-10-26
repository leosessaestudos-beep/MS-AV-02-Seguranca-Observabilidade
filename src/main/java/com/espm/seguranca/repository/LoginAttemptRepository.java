package com.espm.seguranca.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Controle de tentativas de login (tabela "login_attempts").
 */
@Repository
public class LoginAttemptRepository {

    private static final String SQL_INCREMENTAR_FALHA =
            "update login_attempts set failed_attempts = failed_attempts + 1, "
                    + "last_failed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP where username = ?";

    private final JdbcTemplate jdbc;
    private final int maxFalhas;

    public LoginAttemptRepository(JdbcTemplate jdbc,
                                  @Value("${seguranca.login.max-falhas:1}") int maxFalhas) {
        this.jdbc = jdbc;
        this.maxFalhas = maxFalhas;
    }

    public boolean estaBloqueado(String username) {
        List<Boolean> resultado = jdbc.queryForList(
                "select locked from login_attempts where username = ?", Boolean.class, username);
        return !resultado.isEmpty() && Boolean.TRUE.equals(resultado.get(0));
    }

    /**
     * Registra uma falha de login. Se o total de falhas ficar MAIOR que o limite
     * configurado (seguranca.login.max-falhas), o usuario e bloqueado.
     *
     * @return true se o usuario foi bloqueado por causa desta falha
     */
    @Transactional
    public boolean registrarFalha(String username) {
        int atualizadas = jdbc.update(SQL_INCREMENTAR_FALHA, username);
        if (atualizadas == 0) {
            try {
                jdbc.update("insert into login_attempts (username, failed_attempts, locked, last_failed_at, updated_at) "
                        + "values (?, 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", username);
            } catch (DuplicateKeyException e) {
                // outra requisicao criou o registro ao mesmo tempo: apenas incrementa
                jdbc.update(SQL_INCREMENTAR_FALHA, username);
            }
        }

        int bloqueados = jdbc.update("update login_attempts set locked = true, locked_at = CURRENT_TIMESTAMP, "
                + "updated_at = CURRENT_TIMESTAMP where username = ? and locked = false and failed_attempts > ?",
                username, maxFalhas);
        return bloqueados > 0;
    }

    /** Zera as falhas apos um login valido (somente se o usuario nao estiver bloqueado). */
    public void registrarSucesso(String username) {
        jdbc.update("update login_attempts set failed_attempts = 0, updated_at = CURRENT_TIMESTAMP "
                + "where username = ? and locked = false", username);
    }

    /** Desbloqueia o usuario e zera o contador de falhas. */
    public void desbloquear(String username) {
        jdbc.update("update login_attempts set locked = false, failed_attempts = 0, locked_at = null, "
                + "updated_at = CURRENT_TIMESTAMP where username = ?", username);
    }

    /** Total de usuarios bloqueados no momento (usado pelo indicador do Prometheus/Grafana). */
    public long contarBloqueados() {
        Long total = jdbc.queryForObject("select count(*) from login_attempts where locked = true", Long.class);
        return total == null ? 0L : total;
    }
}
