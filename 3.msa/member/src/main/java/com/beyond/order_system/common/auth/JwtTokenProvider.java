package com.beyond.order_system.common.auth;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {
    /* *********************** JWT 설정 *********************** */
    @Value("${jwt.secretKey}")
    private String st_secret_key;

    @Value("${jwt.expiration}")
    private int exp_minuet;

    private Key secret_key;

    @Value("${jwt.secretKeyRt}")
    private String st_secret_key_rt;

    @Value("${jwt.expirationRt}")
    private int exp_minuet_rt;

    private Key secret_key_rt;

    /* *********************** DI 주입 *********************** */
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;

    @Autowired
    public JwtTokenProvider(@Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate,
                            MemberRepository memberRepository) {
        this.redisTemplate = redisTemplate;
        this.memberRepository = memberRepository;
    }

    @PostConstruct
    public void init() {
        secret_key = new SecretKeySpec(Base64.getDecoder().decode(st_secret_key), SignatureAlgorithm.HS512.getJcaName());
        secret_key_rt = new SecretKeySpec(Base64.getDecoder().decode(st_secret_key_rt), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createAtToken(Member member) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(member.getEmail()));
        claims.put("role", member.getRole().toString());

        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + exp_minuet * 60 * 1000L))
                .signWith(secret_key)
                .compact();
        return token;
    }

    public String createRtToken(Member member) {
        // 1. 유효 기간이 긴 RT 토큰 생성
        Claims claims = Jwts.claims().setSubject(String.valueOf(member.getEmail()));
        claims.put("role", member.getRole().toString());

        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + exp_minuet_rt * 60 * 1000L))
                .signWith(secret_key_rt)
                .compact();

        // 2. 생성한 RT토큰 Redis에 저장
        // opsForSet, opsForZset, opsForList 등 value에 들어올 수 있는 자료구조들이 있다.
        // opsForValue는 일반 String 자료구조를 저장할 때 사용
        redisTemplate.opsForValue().set(member.getEmail(), token, exp_minuet_rt, TimeUnit.MINUTES);
        return token;
    }

    public Member validateRt(String refreshToken) {
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(secret_key_rt)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 토큰입니다.");
        }

        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("entity not found"));

        String redisRt = redisTemplate.opsForValue().get(email);
        if (redisRt != null && !redisRt.equals(refreshToken)) {
            throw new IllegalArgumentException("잘못된 토큰입니다.");
        }
        return member;
    }

}