package com.beyond.order_system.member.dto.response;

import com.beyond.order_system.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MemberListResDto {

    private List<MemberSummary> members;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class MemberSummary {
        private Long id;
        private String name;
        private String email;

        public static MemberSummary fromEntity(Member member) {
            return MemberSummary.builder()
                    .id(member.getId())
                    .name(member.getName())
                    .email(member.getEmail())
                    .build();
        }
    }

    public static MemberListResDto fromEntity(List<Member> members) {
        return MemberListResDto.builder()
                .members(members.stream().map(MemberSummary::fromEntity).toList())
                .build();
    }
}
