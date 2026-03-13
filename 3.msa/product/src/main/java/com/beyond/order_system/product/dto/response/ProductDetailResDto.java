package com.beyond.order_system.product.dto.response;

import com.beyond.order_system.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductDetailResDto {
    private Long id;
    private String name;
    private String category;
    private Double price;
    private Long stockQuantity;
    private String imagePath;

    public static ProductDetailResDto fromEntity(Product product) {
        return ProductDetailResDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imagePath(product.getImagePath())
                .build();
    }
}
