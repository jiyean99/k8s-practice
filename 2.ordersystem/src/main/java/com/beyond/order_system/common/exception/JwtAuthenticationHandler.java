package com.beyond.order_system.common.exception;

import com.beyond.order_system.common.dto.CommonErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtAuthenticationHandler implements AuthenticationEntryPoint {
    /* *********************** DI주입 *********************** */
    private final ObjectMapper objectMapper;

    @Autowired
    public JwtAuthenticationHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /* *********************** JwtAuthenticationHandler *********************** */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        authException.printStackTrace();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String data = objectMapper.writeValueAsString(CommonErrorDto.builder()
                .status_code(401).error_message("토큰이 없거나 유효하지 않습니다.").build());
        PrintWriter printWriter = response.getWriter();
        printWriter.write(data);
        printWriter.flush();
    }
}
