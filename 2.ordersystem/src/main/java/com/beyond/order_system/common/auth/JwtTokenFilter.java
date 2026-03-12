package com.beyond.order_system.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtTokenFilter extends GenericFilter {
    /* *********************** JWT 설정 *********************** */
    @Value("${jwt.secretKey}")
    private String st_secret_key;


    /* *********************** 토큰 검증 및 인증객체 생성 *********************** */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException  {
        try {

            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            String bearerToken = httpServletRequest.getHeader("Authorization");

            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                chain.doFilter(request, response);
                return;
            }

            String token = bearerToken.substring(7);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(st_secret_key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        }catch (Exception e){
            // 실제 에러가 아닌 요소들은 로그를 찍을 필요가 없으므로 아래의 printStackTrace 주석처리
            // e.printStackTrace();
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
