package com.beyond.order_system.ordering.dto.response;

import com.beyond.order_system.ordering.domain.OrderStatus;
import com.beyond.order_system.ordering.domain.OrderingDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MyOrdersResDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderingDetails> ordersDetail;
}
