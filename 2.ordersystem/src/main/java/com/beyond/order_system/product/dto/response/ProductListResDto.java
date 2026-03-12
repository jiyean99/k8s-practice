package com.beyond.order_system.product.dto.response;

import com.beyond.order_system.product.domain.Product;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductListResDto {

    private List<ProductListItem> content;

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class ProductListItem {
        private Long id;
        private String name;
        private String category;
        private Double price;
        private Long stockQuantity;
        private String imagePath;

        public static ProductListItem fromEntity(Product product) {
            return ProductListItem.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .category(product.getCategory())
                    .price(product.getPrice())
                    .stockQuantity(product.getStockQuantity())
                    .imagePath(product.getImagePath())
                    .build();
        }
    }
}
