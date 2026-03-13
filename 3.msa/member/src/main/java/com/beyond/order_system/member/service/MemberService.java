package com.beyond.order_system.member.service;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.dto.request.MemberCreateReqDto;
import com.beyond.order_system.member.dto.request.MemberLoginReqDto;
import com.beyond.order_system.member.dto.response.MemberDetailResDto;
import com.beyond.order_system.member.dto.response.MemberListResDto;
import com.beyond.order_system.member.dto.response.MyInfoResDto;
import com.beyond.order_system.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class MemberService {
    /* *********************** DI주입 *********************** */
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* *********************** 서비스 *********************** */
    // 회원가입(쓰기)
    public void save(MemberCreateReqDto dto) {
        Member member = dto.toEntity(passwordEncoder.encode(dto.getPassword()));

        if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        memberRepository.save(member);
    }

    // 로그인
    @Transactional(readOnly = true)
    public Member login(MemberLoginReqDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        return member;
    }

    // 회원 목록 조회
    @Transactional(readOnly = true)
    public MemberListResDto findAll() {
        List<Member> members = memberRepository.findAll();
        return MemberListResDto.fromEntity(members);
    }

    // 내 정보 조회
    @Transactional(readOnly = true)
    public MyInfoResDto myInfo(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일이 입력되지 않았습니다.");
        }

        Member member = memberRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("등록된 사용자가 없습니다."));

        return MyInfoResDto.fromEntity(member);
    }


    // 회원 상세 조회
    @Transactional(readOnly = true)
    public MemberDetailResDto findById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        return MemberDetailResDto.fromEntity(member);
    }
}
