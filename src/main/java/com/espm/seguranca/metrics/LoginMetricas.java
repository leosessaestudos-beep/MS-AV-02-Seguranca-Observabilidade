package com.espm.seguranca.metrics;

import com.espm.seguranca.repository.LoginAttemptRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Indicadores de login expostos em /actuator/prometheus:
 *
 *   logins_validos_total    (counter) - quantidade de logins validos
 *   logins_invalidos_total  (counter) - quantidade de logins invalidos
 *   usuarios_bloqueados     (gauge)   - total de usuarios bloqueados no momento
 *
 * O total de bloqueados e lido da tabela login_attempts a cada coleta (scrape),
 * por isso diminui automaticamente quando um administrador desbloqueia alguem.
 */
@Component
public class LoginMetricas {

    private final Counter loginsValidos;
    private final Counter loginsInvalidos;

    public LoginMetricas(MeterRegistry registry, LoginAttemptRepository loginAttemptRepository) {
        this.loginsValidos = Counter.builder("logins.validos")
                .description("Quantidade de logins validos")
                .register(registry);

        this.loginsInvalidos = Counter.builder("logins.invalidos")
                .description("Quantidade de logins invalidos")
                .register(registry);

        Gauge.builder("usuarios.bloqueados", loginAttemptRepository, LoginAttemptRepository::contarBloqueados)
                .description("Total de usuarios bloqueados")
                .register(registry);
    }

    public void registrarLoginValido() {
        loginsValidos.increment();
    }

    public void registrarLoginInvalido() {
        loginsInvalidos.increment();
    }
}
