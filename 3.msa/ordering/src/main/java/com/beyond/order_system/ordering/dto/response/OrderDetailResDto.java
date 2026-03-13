package com.beyond.order_system.ordering.dto.response;

import com.beyond.order_system.ordering.domain.OrderingDetails;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderDetailResDto {
    private Long detailId;
    private String productName;
    private Long productCount;

    // OrderingDetails 엔티티를 DTO로 변환
    public static OrderDetailResDto fromEntity(OrderingDetails details) {
        if (details == null) {
            return null;
        }
        return OrderDetailResDto.builder()
                .detailId(details.getId())
                .productName(details.getProductName())
                .productCount(details.getQuantity() != null ?
                        details.getQuantity().longValue() : 0L)
                .build();
    }
}