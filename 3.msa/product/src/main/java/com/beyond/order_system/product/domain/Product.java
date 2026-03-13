package com.beyond.order_system.product.domain;

import com.beyond.order_system.product.dto.request.ProductUpdateReqDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String memberEmail;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    private String category;

    @Column(nullable = false)
    private Long stockQuantity;

    @Column(name = "image_path")
    private String imagePath;

    @Builder.Default
    private LocalDateTime createdTime = LocalDateTime.now();

    public void updateImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void decreaseStockQuantity(long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("감소 수량은 1 이상이어야 합니다.");
        }

        long rest = this.stockQuantity - quantity;
        if (rest < 0) {
            throw new IllegalArgumentException("재고 부족");
        }

        this.stockQuantity = rest;
    }

    public void updateProduct(ProductUpdateReqDto dto) {
        this.name = dto.getName();
        this.category = dto.getCategory();
        this.stockQuantity = dto.getStockQuantity();
        this.price = dto.getPrice();
    }
}
