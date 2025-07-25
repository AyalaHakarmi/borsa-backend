package com.burse.bursebackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI burseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Burse Trading API")
                        .description("API documentation for the stock exchange system")
                        .version("v1.0"));
    }
}


