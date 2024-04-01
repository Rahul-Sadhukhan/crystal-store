package com.walmart.realestate.crystal.storereview.config;

import com.walmart.core.realestate.cerberus.constants.CerberusConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearer", bearerSecurityScheme())
                        .addSecuritySchemes("cerberus", cerberusSecurityScheme()))
                .addSecurityItem(new SecurityRequirement().addList("bearer").addList("cerberus"));
    }

    private SecurityScheme bearerSecurityScheme() {
        return new SecurityScheme()
                .name(HttpHeaders.AUTHORIZATION)
                .scheme("Bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .type(SecurityScheme.Type.HTTP);
    }

    private SecurityScheme cerberusSecurityScheme() {
        return new SecurityScheme()
                .name(CerberusConstants.CERBERUS_AUTH_HEADER_NAME)
                .in(SecurityScheme.In.HEADER)
                .type(SecurityScheme.Type.APIKEY);
    }

}