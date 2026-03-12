package com.beyond.order_system.ordering.dto.response;

import com.beyond.order_system.ordering.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    }
}
