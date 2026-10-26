package com.espm.seguranca.service;

import com.espm.seguranca.dto.NovoUsuarioRequest;
import com.espm.seguranca.dto.TrocaSenhaRequest;
import com.espm.seguranca.dto.UsuarioResponse;
import com.espm.seguranca.exception.SenhaAtualInvalidaException;
import com.espm.seguranca.exception.UsuarioBloqueadoException;
import com.espm.seguranca.exception.UsuarioJaExisteException;
import com.espm.seguranca.exception.UsuarioNaoEncontradoException;
import com.espm.seguranca.repository.LoginAttemptRepository;
import com.espm.seguranca.repository.UsuarioRepository;
import com.espm.seguranca.security.SenhaUtil;
import com.espm.seguranca.security.UsuariosUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private static final String ROLE_PADRAO = "USER";

    private final UsuarioRepository usuarioRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final UsuariosUserDetailsManager userDetailsManager;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          LoginAttemptRepository loginAttemptRepository,
                          UsuariosUserDetailsManager userDetailsManager) {
        this.usuarioRepository = usuarioRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.userDetailsManager = userDetailsManager;
    }

    /** Cadastra um novo usuario (com a role ROLE_USER) usando o JdbcUserDetailsManager. */
    @Transactional
    public UsuarioResponse criar(NovoUsuarioRequest request) {
        if (usuarioRepository.existe(request.username())) {
            throw new UsuarioJaExisteException(request.username());
        }

        UserDetails novoUsuario = User.withUsername(request.username())
                .password(request.senha())
                .roles(ROLE_PADRAO)
                .build();
        userDetailsManager.createUser(novoUsuario);

        return new UsuarioResponse(request.username(), List.of("ROLE_" + ROLE_PADRAO), false, 0);
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.listar();
    }

    /**
     * Troca a senha apos conferir a senha atual na base de dados.
     *
     * Observacao de seguranca: como o endpoint e publico, uma senha atual incorreta
     * tambem conta como falha de login (e pode bloquear o usuario). Sem isso, o
     * endpoint permitiria testar senhas indefinidamente, contornando o bloqueio.
     * (Este metodo nao e @Transactional de proposito: o registro da falha precisa
     * ser confirmado mesmo quando a excecao e lancada logo em seguida.)
     */
    public void trocarSenha(String username, TrocaSenhaRequest request) {
        if (!usuarioRepository.existe(username)) {
            throw new UsuarioNaoEncontradoException(username);
        }
        if (loginAttemptRepository.estaBloqueado(username)) {
            throw new UsuarioBloqueadoException(username);
        }

        String senhaCadastrada = usuarioRepository.buscarSenha(username);
        if (!SenhaUtil.iguais(senhaCadastrada, request.atual())) {
            loginAttemptRepository.registrarFalha(username);
            throw new SenhaAtualInvalidaException();
        }

        usuarioRepository.atualizarSenha(username, request.nova());
        loginAttemptRepository.registrarSucesso(username);
    }

    /** Desbloqueia o usuario e zera o contador de falhas. */
    public void desbloquear(String username) {
        if (!usuarioRepository.existe(username)) {
            throw new UsuarioNaoEncontradoException(username);
        }
        loginAttemptRepository.desbloquear(username);
    }
}
