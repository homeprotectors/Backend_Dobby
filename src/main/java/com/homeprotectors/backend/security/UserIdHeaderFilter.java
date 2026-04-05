package com.homeprotectors.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeprotectors.backend.dto.common.ResponseDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class UserIdHeaderFilter extends OncePerRequestFilter {

    public static final String ATTR_CURRENT_USER_ID = "currentUserId";
    private static final String HEADER_USER_ID = "X-USER-ID";
    private final ObjectMapper objectMapper;

    public UserIdHeaderFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Public endpoints that should stay accessible without a user header.
    private final Set<String> publicPaths = Set.of(
            "/api/guests/register",
            "/api/admin/notifications/daily-chore-reminder/dispatch",
            "/api/admin/notifications/daily-chore-reminder/test-dispatch",
            "/actuator/health"
    );

    // Springdoc uses path prefixes for UI assets and grouped docs endpoints.
    private final Set<String> publicPathPrefixes = Set.of(
            "/swagger-ui",
            "/v3/api-docs"
    );

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

        String raw = request.getHeader(HEADER_USER_ID);
        if (raw == null || raw.isBlank()) {
            writeBadRequest(response, "X-USER-ID header is required.");
            return;
        }

        UUID userId;
        try {
            userId = UUID.fromString(raw.trim());
        } catch (Exception e) {
            writeBadRequest(response, "X-USER-ID must be a valid UUID.");
            return;
        }

        request.setAttribute(ATTR_CURRENT_USER_ID, userId);
        filterChain.doFilter(request, response);
    }

    private void writeBadRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ResponseDTO<Object> body = new ResponseDTO<>(false, message, null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
