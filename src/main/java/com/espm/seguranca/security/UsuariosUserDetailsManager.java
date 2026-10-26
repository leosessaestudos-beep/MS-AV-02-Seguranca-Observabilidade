package com.espm.seguranca.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import javax.sql.DataSource;

/**
 * JdbcUserDetailsManager adaptado ao modelo de dados do enunciado.
 *
 * Em vez do schema padrao do Spring (tabelas "users" e "authorities"), as
 * credenciais ficam na tabela "usuarios" e a situacao de bloqueio vem da
 * tabela "login_attempts" (usuario bloqueado => enabled = false, e por isso
 * ele tambem nao consegue se autenticar nos endpoints protegidos).
 * As roles continuam na tabela "authorities".
 */
public class UsuariosUserDetailsManager extends JdbcUserDetailsManager {

    private static final String SQL_USUARIO = """
            select u.username,
                   coalesce(u.senha, '') as senha,
                   case when l.locked = true then false else true end as enabled
            from usuarios u
            left join login_attempts l on l.username = u.username
            where u.username = ?
            """;

    private static final String SQL_AUTHORITIES =
            "select username, authority from authorities where username = ?";

    private static final String SQL_USUARIO_EXISTE =
            "select username from usuarios where username = ?";

    public UsuariosUserDetailsManager(DataSource dataSource) {
        super();
        setDataSource(dataSource);
        setUsersByUsernameQuery(SQL_USUARIO);
        setAuthoritiesByUsernameQuery(SQL_AUTHORITIES);
        setUserExistsSql(SQL_USUARIO_EXISTE);
    }

    /**
     * Cria o usuario na tabela "usuarios" e suas roles na tabela "authorities".
     * (O metodo original gravaria na tabela padrao "users".)
     */
    @Override
    public void createUser(UserDetails user) {
        JdbcTemplate jdbc = getJdbcTemplate();
        jdbc.update("insert into usuarios (username, senha) values (?, ?)", user.getUsername(), user.getPassword());
        for (GrantedAuthority authority : user.getAuthorities()) {
            jdbc.update("insert into authorities (username, authority) values (?, ?)",
                    user.getUsername(), authority.getAuthority());
        }
    }
}
