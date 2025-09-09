package com.guidely.chatorchestra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.ArrayList;

/**
 * OpenAPI configuration for Swagger documentation
 */
@Configuration
public class OpenApiConfig {
    
    @Value("${server.port:8081}")
    private String serverPort;
    
    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();
        
        // 운영 서버 (Azure)
        servers.add(new Server()
                .url("https://yerak-chat-cyfze4hnhbeaawc8.koreacentral-01.azurewebsites.net")
                .description("Production server (Azure)"));
        
        // 개발 서버 (로컬)
        servers.add(new Server()
                .url("http://localhost:" + serverPort)
                .description("Development server"));
        
        return new OpenAPI()
                .info(new Info()
                        .title("Chat-Orchestra API")
                        .description("Pure MSA conversation management service with MySQL integration")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Chat-Orchestra Team")
                                .email("support@guidely.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(servers);
    }
}
