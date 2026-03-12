package com.beyond.order_system.common.config;

import com.beyond.order_system.common.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;
    /*
     * [연결 빈객체 : redisConnectionFactory]
     * - redis에 대한 연결 정보(Host, Port, DB 번호)
     *
     * [템플릿 빈객체]
     * - 자료구조 설계
     * - 이 때 Bean 객체로 redisConnectionFactory를 주입받고 있다.
     * - 파라미터로 받고 있으며 이는 빈객체간의 DI 방식이다.
     *
     * [@Qualifier]
     * - 같은 Bean 객체가 여러개 있을 경우, Bean객체를 구분하기 위한 어노테이션
     * */

    /* *********************** Redis 연결 *********************** */
    // 연결 빈객체
    @Bean
    @Qualifier("rtInventory")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);
        return new LettuceConnectionFactory(configuration);
    }

    // 템플릿 빈객체
    @Bean
    @Qualifier("rtInventory")
    // 모든 template 중에 무조건 redisTemplate이라는 메서드명이 반드시 한 개는 있어야함.
    // Bean객체 생성 시, Bean 객체간에 DI(의존성 주입)는 "메서드 파라미터 주입"이 가능하다.
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        // key와 value를 String으로 만들어서 저장하겠다는 설정(내부적으로 자료구조에 대한 태깅은 갖고있다)
        // setKeySerializer : 직렬화의 도구
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        // 우리가 생성한 연결 빈 객체를 넘겨주는 작업
        // 매개변수로 주입받는 독특한 형태
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    // 연결 빈객체
    @Bean
    @Qualifier("stockInventory")
    public RedisConnectionFactory redisStockConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);
        return new LettuceConnectionFactory(configuration);
    }

    // 템플릿 빈객체
    @Bean
    @Qualifier("stockInventory")
    public RedisTemplate<String, String> redisStockTemplate(@Qualifier("stockInventory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    /* *********************** SSE PUB/SUB 세팅 *********************** */
    // 연결 빈객체
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory ssePubSubConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        // redis pub/sub 기능은 DB에 값을 저장하는 기능이 아니므로, 특정 DB에 의존적이지 않음.
        return new LettuceConnectionFactory(configuration);
    }

    // 템플릿 빈객체
    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String> redisSsePubSubTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    // redis 리스너(subscribe) 객체
    // 호출 구조:
    // RedisMessageListenerContainer -> messageListenerAdapter -> SseAlarmService(MessageListener 구현)
    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory, @Qualifier("ssePubSub") MessageListenerAdapter messageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel"));
        // 만약에 여러 채널을 구독해야하는 경우, 여러개의 PatterTopic을 add 하거나, 별도의 Listener Bean 객체 생성
        return container;
    }

    // redis에서 수신된 메시지를 처리하는 객체
    @Bean
    @Qualifier("ssePubSub")
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService) {
        // 채널로부터 수신되는 message 처리를 SseAlarmService의 onMessage 메서드로 위임
        return new MessageListenerAdapter(sseAlarmService, "onMessage");
    }
}
