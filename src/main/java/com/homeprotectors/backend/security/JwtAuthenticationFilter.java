package com.homeprotectors.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.exception.ApiException;
import com.homeprotectors.backend.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ATTR_CURRENT_USER_ID = "currentUserId";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper;
    private final JwtTokenService jwtTokenService;

    private final Set<String> publicPaths = Set.of(
            "/api/guests/register",
            "/api/auth/refresh",
            "/api/admin/notifications/daily-chore-reminder/dispatch",
            "/api/admin/notifications/daily-chore-reminder/test-dispatch",
            "/actuator/health"
    );

    private final Set<String> publicPathPrefixes = Set.of(
            "/swagger-ui",
            "/v3/api-docs"
    );

    public JwtAuthenticationFilter(ObjectMapper objectMapper, JwtTokenService jwtTokenService) {
        this.objectMapper = objectMapper;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (publicPaths.contains(path)) {
            return true;
        }

        return publicPathPrefixes.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            writeUnauthorized(response, "Authorization header is required.");
            return;
        }

        if (!authorization.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response, "Authorization header must use Bearer token.");
            return;
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            writeUnauthorized(response, "Bearer token is required.");
            return;
        }

        try {
            UUID userId = jwtTokenService.parseAccessToken(token);
            request.setAttribute(ATTR_CURRENT_USER_ID, userId);
            filterChain.doFilter(request, response);
        } catch (ApiException e) {
            writeUnauthorized(response, e.getMessage());
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ResponseDTO<Object> body = new ResponseDTO<>(false, message, null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
