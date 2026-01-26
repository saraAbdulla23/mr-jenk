package com.user_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class ForbiddenHandler implements AccessDeniedHandler {

    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException exception) throws IOException, ServletException {

        String requestPath = request.getRequestURI();
        String denialReason;

        if ("/api/users".equalsIgnoreCase(requestPath) && "DELETE".equals(request.getMethod())) {
            denialReason = "Only admins are allowed to delete users.";
        } else {
            denialReason = "Access denied. You lack the necessary permissions.";
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", LocalDateTime.now().toString());
        payload.put("status", HttpServletResponse.SC_FORBIDDEN);
        payload.put("error", "Forbidden");
        payload.put("message", denialReason);
        payload.put("path", requestPath);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        jsonMapper.writeValue(response.getOutputStream(), payload);
    }
}
