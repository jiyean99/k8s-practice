package com.beyond.order_system.ordering.domain;

import com.beyond.order_system.member.domain.Member;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

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
        item.setOrdering(this);
    }
}
