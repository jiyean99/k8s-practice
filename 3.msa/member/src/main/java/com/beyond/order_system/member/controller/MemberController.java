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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public MemberListResDto findAll() {
        return memberService.findAll();
    }

    // 내 정보 조회
    // “X-”로 시작하는 헤더명은 개발자가 인위적으로 만든 Header인 경우에 관례적으로 사용
    @GetMapping("/myinfo")
    public ResponseEntity<?> myInfo(@RequestHeader("X-User-Email") String email) {
        System.out.println(email);
        MyInfoResDto dto = memberService.myInfo(email);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    // 회원 상세 조회
    @GetMapping("/detail/{id}")
    public MemberDetailResDto findById(@PathVariable Long id) {
        return memberService.findById(id);
    }

    @PostMapping("/refresh-at")
    public ResponseEntity<?> refreshAt(@RequestBody RefreshTokenReqDto dto) {
        Member member = jwtTokenProvider.validateRt(dto.getRefreshToken());

        String accessToken = jwtTokenProvider.createAtToken(member);
        MemberLoginResDto tokenDto = MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(dto.getRefreshToken()) // null로 둬도 됨
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(tokenDto);
    }
}
