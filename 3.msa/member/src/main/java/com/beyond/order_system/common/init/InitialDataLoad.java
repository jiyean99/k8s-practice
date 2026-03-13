package com.beyond.order_system.common.init;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.domain.Role;
import com.beyond.order_system.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class InitialDataLoad implements CommandLineRunner {
    /* *********************** DI주입 *********************** */
    private final MemberRepository authorRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public InitialDataLoad(MemberRepository authorRepository, PasswordEncoder passwordEncoder) {
        this.authorRepository = authorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* *********************** 서버 실행 시 어드민 계정 주입 *********************** */
    @Override
    public void run(String... args) throws Exception {
        if (authorRepository.findByEmail("admin@naver.com").isPresent()) {
            return;
        }

        authorRepository.save(Member.builder()
                .name("admin")
                .email("admin@naver.com")
                .role(Role.ADMIN)
                .password(passwordEncoder.encode("admin"))
                .build());
    }
}