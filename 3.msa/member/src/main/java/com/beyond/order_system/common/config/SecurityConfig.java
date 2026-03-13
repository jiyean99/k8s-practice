package com.beyond.order_system.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/*
* TODO [기존 모놀리식 아키텍쳐에서의 SecurityConfig 주요 작업]
* - CORS 처리
* - 토큰 필터 코드 내 토큰검증 후 인증객체 생성(예외처리)
*   해당 작업을 API GATEWAY, 즉 앞단에서 수행할 것
* */
@Configuration
public class SecurityConfig {
    /* *********************** 비밀번호 암호화 *********************** */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
