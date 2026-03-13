package com.beyond.order_system.common.service;

import com.beyond.order_system.common.dto.SseMessageDto;
import com.beyond.order_system.common.repository.SseEmitterRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class SseAlarmService implements MessageListener {
    /* *********************** DI 주입 *********************** */
    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry,
                           ObjectMapper objectMapper,
                           @Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public void sendMessage(String receiverEmail, String senderEmail, String message) {

        SseMessageDto dto = SseMessageDto.builder()
                .receiverEmail(receiverEmail)
                .senderEmail(senderEmail)
                .message(message)
                .build();

        String data = null;

        try {
            data = objectMapper.writeValueAsString(dto);
            // [기존코드] : 데이터를 사용자에게 바로 전파
            // sseEmitter.send(SseEmitter.event().name("ordered").data(data));

            // [개선코드] : sseEmitter 객체가 있으면 데이터를 바로 던져주고(알림발송), 없으면 redis의 pub/sub에 던지는 방식으로 개선
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(receiverEmail);
            if (sseEmitter != null) {
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
                // 사용자가 새로고침 후에 알림메시지를 조회하려면 DB에 추가적으로 저장 필요.(이는 SSE 기술과 무관한 시나리오상 필요한 작업)
            } else {
                redisTemplate.convertAndSend("order-channel", data); // redis pub/sub의 기능을 활용하여 메시지 Publish
            }

        } catch (IOException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // message: 실질적으로 메시지가 담겨있는 객체
        // pattern: 채널명(redis에서 관리하는 논리적인 공간, 물리적 공간 X)
        // 추후 여러개의 채널에 각기 메시지를 publish 하고, subscribe할 경우 채널명의 분기가 가능하다.
        String channelName = new String(pattern);
        try {
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class);
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(dto.getReceiverEmail());

            String data = objectMapper.writeValueAsString(dto);
            if (sseEmitter != null) { // 해당 서버에 receiver의 emitter 객체가 있으면 send
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
            }
        } catch (IOException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
        }
    }
}
