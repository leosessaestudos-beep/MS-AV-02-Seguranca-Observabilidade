-- ---------------------------------------------------------------------------
-- Tabelas definidas no enunciado (MS-AV-02)
-- OBS: o script original do enunciado tinha uma virgula sobrando depois da
-- coluna "updated_at" em login_attempts (erro de sintaxe) - corrigida aqui.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS login_attempts (
    username VARCHAR(100) PRIMARY KEY,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    last_failed_at TIMESTAMP NULL,
    locked_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuarios (
    username VARCHAR(100) PRIMARY KEY,
    senha VARCHAR(10)
);

-- ---------------------------------------------------------------------------
-- Tabela de roles usada pelo JdbcUserDetailsManager (authorities).
-- Os usuarios e senhas ficam na tabela "usuarios" (o JdbcUserDetailsManager
-- e configurado para ler dela, e nao da tabela padrao "users").
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS authorities (
    username VARCHAR(100) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    PRIMARY KEY (username, authority),
    CONSTRAINT fk_authorities_usuarios FOREIGN KEY (username) REFERENCES usuarios(username)
);
