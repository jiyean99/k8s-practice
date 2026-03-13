package com.beyond.order_system.common.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {
    /*
     * [sse emitter 객체]
     * - 사용자의 연결정보(ip, mac address 등)를 의미
     *
     * [ConcurrentHashMap]
     * - 스레드 세이프한 구조의 map으로 동시성 발생 X(synchronized)
     * */

    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public void addSseEmitter(String email, SseEmitter sseEmitter) {
        this.emitterMap.put(email, sseEmitter);
        System.out.println(this.emitterMap.size());
    }

    public SseEmitter getEmitter(String email) {
        return this.emitterMap.get(email);
    }

    public void removeSseEmitter(String email) {
        this.emitterMap.remove(email);
        System.out.println(this.emitterMap.size());
    }

}
