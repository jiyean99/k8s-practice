package com.beyond.order_system.ordering.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderCreateReqDto {
    @Valid
    @NotNull
    private List<OrderItemCreateReqDto> items;
}
