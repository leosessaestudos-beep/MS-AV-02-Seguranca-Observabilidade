package com.espm.seguranca.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API de Login - Seguranca e Observabilidade",
        version = "1.0.0",
        description = "MS-AV-02: login com bloqueio por tentativas, controle de acesso por roles "
                + "(JdbcUserDetailsManager) e indicadores expostos para Prometheus/Grafana."))
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "Autenticacao HTTP Basic com usuario e senha cadastrados na tabela usuarios")
public class OpenApiConfig {
}
