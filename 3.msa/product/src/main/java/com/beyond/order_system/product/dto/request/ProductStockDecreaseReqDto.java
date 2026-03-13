package com.beyond.order_system.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductStockDecreaseReqDto {
    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    private Long productCount;
}
