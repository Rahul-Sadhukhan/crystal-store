package com.walmart.realestate.crystal.storereview.client.estr;

import com.walmart.realestate.crystal.storereview.tenant.TenantContext;
import feign.RequestInterceptor;
import io.jsonwebtoken.Jwts;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class EstrClientConfiguration {

    private static final String ESTR_USER_HEADER = "Estr-User";

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> getHeaders().forEach(template::header);
    }

    private String buildToken(Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("roles", roles)
                .compact();
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).ifPresent(authentication -> headers.put(ESTR_USER_HEADER, buildToken(authentication)));
        headers.put("x-tenant", TenantContext.getCurrentTenant());
        return headers;
    }

}
