package com.beyond.order_system.member.dto.response;

import com.beyond.order_system.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MemberDetailResDto {
    private Long id;
    private String name;
    private String email;

    public static MemberDetailResDto fromEntity(Member member) {
        return MemberDetailResDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}
