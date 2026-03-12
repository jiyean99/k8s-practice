package com.beyond.order_system.member.controller;

import com.beyond.order_system.common.auth.JwtTokenProvider;
import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.dto.request.MemberCreateReqDto;
import com.beyond.order_system.member.dto.request.MemberLoginReqDto;
import com.beyond.order_system.member.dto.request.RefreshTokenReqDto;
import com.beyond.order_system.member.dto.response.MemberDetailResDto;
import com.beyond.order_system.member.dto.response.MemberListResDto;
import com.beyond.order_system.member.dto.response.MemberLoginResDto;
import com.beyond.order_system.member.dto.response.MyInfoResDto;
import com.beyond.order_system.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
public class MemberController {
    /* *********************** DI주입 *********************** */
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /* *********************** 컨트롤러 *********************** */
    // 회원가입
    @PostMapping("/create")
    @Operation(
            summary = "회원가입", description = "이메일, 비밀번호를 통한 회원가입"
    )
    public ResponseEntity<?> create(@RequestBody @Valid MemberCreateReqDto dto) {
        memberService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("OK");
    }

    // 로그인
    @PostMapping("/doLogin")
    public ResponseEntity<?> login(@RequestBody @Valid MemberLoginReqDto dto) {
        Member member = memberService.login(dto);
        String accessToken = jwtTokenProvider.createAtToken(member);
        // refresh token 생성 및 저장
        String refreshToken = jwtTokenProvider.createRtToken(member);
        MemberLoginResDto tokenDto = MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(tokenDto);
    }

    // 회원 목록 조회
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public MemberListResDto findAll() {
        return memberService.findAll();
    }

    // 내 정보 조회
    @GetMapping("/myinfo")
    public ResponseEntity<?> myInfo(@AuthenticationPrincipal String principal) {
        MyInfoResDto dto = memberService.myInfo(principal); // principal = "memberId"
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    // 회원 상세 조회
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MemberDetailResDto findById(@PathVariable Long id) {
        return memberService.findById(id);
    }

    @PostMapping("/refresh-at")
    public ResponseEntity<?> refreshAt(@RequestBody RefreshTokenReqDto dto){
        // 1. RT 검증 (validateRt: 토큰 자체검증 -> Redis 조회 검증)
        // - 현재 Redis에서 TTL 작업을 수행하지 않았기 때문에 토큰 자체 검증까지 수행하는것이다. (물론 TTL 설정도 할것이다)
        // - 이 때 member 객체를 만들기 위해 claims 를 꺼내면서 토큰은 자체검증이 됨
        Member member = jwtTokenProvider.validateRt(dto.getRefreshToken());

        // 2. AT 신규 생성
        String accessToken = jwtTokenProvider.createAtToken(member);
        MemberLoginResDto tokenDto = MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(dto.getRefreshToken()) // null로 둬도 됨
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(tokenDto);
    }
}
