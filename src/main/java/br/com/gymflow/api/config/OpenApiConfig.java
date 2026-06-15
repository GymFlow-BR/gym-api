package br.com.gymflow.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gymFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GymFlow API")
                        .description("API do MVP Gymflow para gerenciamento de organizações, exercícios, treinos e atribuição de treinos a alunos.")
                        .version("v1")
                );
    }
}