package com.espm.seguranca.service;

import com.espm.seguranca.metrics.LoginMetricas;
import com.espm.seguranca.repository.LoginAttemptRepository;
import com.espm.seguranca.repository.UsuarioRepository;
import com.espm.seguranca.security.SenhaUtil;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UsuarioRepository usuarioRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final LoginMetricas metricas;

    public LoginService(UsuarioRepository usuarioRepository,
                        LoginAttemptRepository loginAttemptRepository,
                        LoginMetricas metricas) {
        this.usuarioRepository = usuarioRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.metricas = metricas;
    }

    /**
     * Valida usuario e senha direto na tabela "usuarios".
     *
     * Regras:
     *  - usuario inexistente ou credenciais ausentes => INVALIDO (nada e gravado em login_attempts)
     *  - usuario bloqueado => BLOQUEADO (mesmo que a senha esteja correta)
     *  - senha incorreta => registra a falha; ao exceder o limite o usuario e bloqueado
     *  - senha correta => zera o contador de falhas
     *
     * Todo resultado diferente de SUCESSO conta como login invalido nos indicadores.
     */
    public ResultadoLogin autenticar(String username, String senha) {
        if (username == null || username.isBlank() || senha == null) {
            metricas.registrarLoginInvalido();
            return ResultadoLogin.INVALIDO;
        }

        if (!usuarioRepository.existe(username)) {
            metricas.registrarLoginInvalido();
            return ResultadoLogin.INVALIDO;
        }

        if (loginAttemptRepository.estaBloqueado(username)) {
            metricas.registrarLoginInvalido();
            return ResultadoLogin.BLOQUEADO;
        }

        String senhaCadastrada = usuarioRepository.buscarSenha(username);
        if (SenhaUtil.iguais(senhaCadastrada, senha)) {
            loginAttemptRepository.registrarSucesso(username);
            metricas.registrarLoginValido();
            return ResultadoLogin.SUCESSO;
        }

        boolean bloqueadoAgora = loginAttemptRepository.registrarFalha(username);
        metricas.registrarLoginInvalido();
        return bloqueadoAgora ? ResultadoLogin.INVALIDO_E_BLOQUEADO : ResultadoLogin.INVALIDO;
    }
}
