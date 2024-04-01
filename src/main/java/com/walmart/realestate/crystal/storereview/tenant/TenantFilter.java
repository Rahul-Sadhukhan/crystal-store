package com.walmart.realestate.crystal.storereview.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.realestate.crystal.storereview.exception.ErrorResponse;
import com.walmart.realestate.crystal.storereview.exception.InvalidTenantException;
import com.walmart.realestate.crystal.storereview.properties.TenantProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

@Component
@Slf4j
@Order(1)
@RequiredArgsConstructor
public class TenantFilter implements Filter {

    private final TenantProperties tenantProperties;

    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String tenantName = req.getHeader("x-tenant");

        if (tenantName != null && !tenantProperties.getTenants().containsKey(tenantName.toUpperCase())) {
            handleInvalidTenantException((HttpServletResponse) response, new InvalidTenantException("Invalid tenantId provided"));
            return;
        }

        TenantContext.setCurrentTenant(tenantName != null ? tenantName.toUpperCase() : tenantProperties.getDefaultTenant());
        chain.doFilter(request, response);
    }

    public void handleInvalidTenantException(HttpServletResponse response, InvalidTenantException e) throws IOException {
        response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .exception(InvalidTenantException.class.getName())
                .message(e.getMessage())
                .error("Unsupported tenantId")
                .build();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
