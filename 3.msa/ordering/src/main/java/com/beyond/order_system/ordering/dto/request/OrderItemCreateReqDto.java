package com.beyond.order_system.ordering.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderItemCreateReqDto {
    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    private Long productCount;
}