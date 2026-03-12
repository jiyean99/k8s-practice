package com.beyond.order_system.ordering.domain;

import com.beyond.order_system.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@ToString(exclude = {"ordering", "product"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderingDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Ordering ordering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Long quantity;

    @Builder.Default
    private LocalDateTime createdTime = LocalDateTime.now();

    public void setOrdering(Ordering ordering) {
        this.ordering = ordering;
    }
}
