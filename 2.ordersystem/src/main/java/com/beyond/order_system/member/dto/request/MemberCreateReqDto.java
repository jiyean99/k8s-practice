package com.beyond.order_system.member.dto.request;


import com.beyond.order_system.member.domain.Member;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MemberCreateReqDto {
    @NotBlank(message = "이름을 작성하시오.")
    private String name;
    @NotBlank(message = "이메일을 작성하시오.")
    private String email;
    @NotBlank(message = "비밀번호을 작성하시오.")
    private String password;

    public Member toEntity(String encodedPassword){
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .password(encodedPassword)
                .build();
    }
}
