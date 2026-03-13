package com.beyond.order_system.common.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    // eureka에 등록된 서비스명을 사용하여 내부 서비스 호출(또는 내부통신)하는 어노테이션
    public RestTemplate makeRestTemplate(){
        return new RestTemplate();
    }
}
