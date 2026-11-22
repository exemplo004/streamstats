package com.example.streamstats.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


// @Configuration para informar ao Spring que esta classe contém configurações
// CORS para controlar quais as páginas podem fazer chamadas HTTP para a API
 
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // @Override para indicar que estamos implementando o método exigido pelo WebMvcConfigurer
    // O objeto registry vai receber as regras de acesso para cada rota 
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }
}
