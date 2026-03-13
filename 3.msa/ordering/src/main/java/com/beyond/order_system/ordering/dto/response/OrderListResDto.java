package com.beyond.order_system.ordering.dto.response;

import com.beyond.order_system.ordering.domain.Ordering;
import com.beyond.order_system.ordering.domain.OrderingDetails;
import com.beyond.order_system.ordering.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderListResDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailResDto> orderDetails;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class OrderDetailResDto {
        private Long detailId;
        private String productName;
        private Long productCount;

        /**
         * OrderingDetails → DTO 변환 (순환참조 해결)
         */
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

    /**
     * Ordering → DTO 변환 (전체 주문 목록용)
     */
    public static OrderListResDto fromEntity(Ordering ordering) {
        if (ordering == null) {
            return null;
        }

        return OrderListResDto.builder()
                .id(ordering.getId())
                .memberEmail(ordering.getMemberEmail())
                .orderStatus(ordering.getOrderStatus())
                .orderDetails(ordering.getOrderItems().stream()
                        .map(OrderDetailResDto::fromEntity)
                        .filter(dto -> dto != null)  // null 안전성
                        .collect(Collectors.toList()))
                .build();
    }
}
