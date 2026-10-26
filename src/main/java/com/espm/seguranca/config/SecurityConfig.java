package com.espm.seguranca.config;

import com.espm.seguranca.security.UsuariosUserDetailsManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * A coluna usuarios.senha e VARCHAR(10) (definida pelo enunciado) e portanto nao
     * comporta um hash BCrypt (60 caracteres). Por isso as senhas sao comparadas em
     * texto puro. Em um sistema real: usar BCrypt e uma coluna VARCHAR(60) ou maior.
     */
    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public UsuariosUserDetailsManager userDetailsService(DataSource dataSource) {
        return new UsuariosUserDetailsManager(dataSource);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // ---- endpoints publicos ----
                        .requestMatchers(antMatcher(HttpMethod.POST, "/login")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.PUT, "/trocarsenha/**")).permitAll()

                        // ---- documentacao (OpenAPI / Swagger) ----
                        .requestMatchers(
                                antMatcher("/v3/api-docs/**"),
                                antMatcher("/v3/api-docs.yaml"),
                                antMatcher("/swagger-ui/**"),
                                antMatcher("/swagger-ui.html")).permitAll()

                        // ---- observabilidade (Prometheus faz scrape sem autenticacao) ----
                        .requestMatchers(antMatcher("/actuator/**")).permitAll()
                        .requestMatchers(antMatcher("/error")).permitAll()

                        // ---- endpoints protegidos por role ----
                        .requestMatchers(antMatcher(HttpMethod.POST, "/")).hasRole("MANUTENCAO")
                        .requestMatchers(antMatcher(HttpMethod.GET, "/")).hasRole("MANUTENCAO")
                        .requestMatchers(antMatcher(HttpMethod.PUT, "/unlock/**")).hasRole("ADMIN")

                        .anyRequest().authenticated());

        return http.build();
    }
}
