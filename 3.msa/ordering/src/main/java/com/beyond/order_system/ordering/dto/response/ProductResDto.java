package com.beyond.order_system.ordering.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProductResDto {
    private Long id;
    private String name;
    private String category;
    private Double price;
    private Long stockQuantity;
    private String imagePath;
}