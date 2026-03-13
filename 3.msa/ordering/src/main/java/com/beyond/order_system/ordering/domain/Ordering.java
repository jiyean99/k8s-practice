package com.beyond.order_system.ordering.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@ToString(exclude = "orderItems")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ordering {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MSA 모듈간의 관계성 제거한 코드로 개선(기존 Member)
    private String memberEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "ordering", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderingDetails> orderItems = new ArrayList<>();

    @Builder.Default
    private LocalDateTime createdTime = LocalDateTime.now();

    public void addItem(OrderingDetails item) {
        orderItems.add(item);
        item.orderingUpdate(this);
    }
}
