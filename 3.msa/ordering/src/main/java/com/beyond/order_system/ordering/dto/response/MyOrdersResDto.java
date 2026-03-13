package com.beyond.order_system.ordering.dto.response;

import com.beyond.order_system.ordering.domain.OrderStatus;
import com.beyond.order_system.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MyOrdersResDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailResDto> ordersDetail;
    private LocalDateTime createdTime;

    // Ordering 엔티티를 DTO로 변환 (순환참조 해결)
    public static MyOrdersResDto fromEntity(Ordering ordering) {
        if (ordering == null) {
            return null;
        }
        return MyOrdersResDto.builder()
                .id(ordering.getId())
                .memberEmail(ordering.getMemberEmail())
                .orderStatus(ordering.getOrderStatus())
                .ordersDetail(ordering.getOrderItems().stream()
                        .map(OrderDetailResDto::fromEntity)
                        .filter(dto -> dto != null) // null 필터링
                        .collect(Collectors.toList()))
                .createdTime(ordering.getCreatedTime())
                .build();
    }
}
