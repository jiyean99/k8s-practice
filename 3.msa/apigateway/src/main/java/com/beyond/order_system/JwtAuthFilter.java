package com.beyond.order_system;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter {

    @Value("${jwt.secretKeyAt}")
    private String secretKey;

    private static final List<String> ALLOWED_PATH = List.of(
            "/member/create",
            "/member/doLogin",
            "/member/refresh-at",
            "/product/list",
            "/health"
    );

    private static final List<String> ADMIN_ONLY_PATH = List.of(
            "/member/list",
            "/member/detail/**",
            "/ordering/list",
            "/product/create"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        // StripPrefix로 접두어를 떼어내었기 때문에 /member/create 등의 url 경로로 들어오게 됨
        String urlPath = exchange.getRequest().getURI().getRawPath();

        // 인증이 필요 없는 경로는 필터 통과
        if (ALLOWED_PATH.contains(urlPath)) {
            return chain.filter(exchange);
        }
        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("token이 없거나, 형식이 잘못되었습니다.");
            }

            String token = bearerToken.substring(7);
            // token검증 및 payload 추출
            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            // admin권한 있어야 하는 url 검증
            if (ADMIN_ONLY_PATH.contains(urlPath) && !role.equals("ADMIN")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // header에 email, role 등 payload값 세팅
            // X를 붙이는 것은 custom header라는 것을 의미하는 관례적 키워드
            // 추후 서비스모듈에서 RequestHeader어노테이션을 사용하여 아래 헤더를 꺼낼 쓸수 있음
            ServerWebExchange serverWebExchange = exchange.mutate()
                    .request(r -> r.header("X-User-Email", email)
                            .header("X-User-Role", role))
                    .build();
            return chain.filter(serverWebExchange);

        } catch (Exception e) {
            e.printStackTrace();
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            // 추가적인 body 메시지는 필요시 세팅
            return exchange.getResponse().setComplete();
        }
    }
}

