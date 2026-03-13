package com.beyond.order_system.ordering.domain;

import com.beyond.order_system.ordering.dto.response.OrderListResDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@ToString(exclude = {"ordering"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderingDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Ordering ordering;

    // MSA 환경에서는 빈번한 HTTP 요청으로 의한 성능저하를 막기 위한 반정규화 설계 가능 (productName 설계의 이유)
    // 모놀리식의 경우 코드참조
    private Long productId;
    private String productName;

    @Column(nullable = false)
    private Long quantity;

    @Builder.Default
    private LocalDateTime createdTime = LocalDateTime.now();

    public void orderingUpdate(Ordering ordering) {
        this.ordering = ordering;
    }
}
