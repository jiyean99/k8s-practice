package com.beyond.order_system.ordering.service;

import com.beyond.order_system.common.service.SseAlarmService;
import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.ordering.domain.OrderStatus;
import com.beyond.order_system.ordering.domain.Ordering;
import com.beyond.order_system.ordering.dto.request.OrderCreateReqDto;
import com.beyond.order_system.ordering.dto.response.OrderListResDto;
import com.beyond.order_system.ordering.repository.OrderingDetailRepository;
import com.beyond.order_system.ordering.repository.OrderingRepository;
import com.beyond.order_system.ordering.domain.OrderingDetails;
import com.beyond.order_system.product.domain.Product;
import com.beyond.order_system.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class OrderingService {
    /* *********************** DI 주입 *********************** */
    private final OrderingRepository orderingRepository;
    private final ProductRepository productRepository;
    private final EntityManager em;
    private final SseAlarmService sseAlarmService;
    private final RedisTemplate<String, String> redisTemplate;
    private final OrderingDetailRepository orderingDetailRepository;
    @Autowired
    public OrderingService(OrderingRepository orderingRepository,
                           ProductRepository productRepository,
                           EntityManager em, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate,
                           OrderingDetailRepository orderingDetailRepository) {
        this.orderingRepository = orderingRepository;
        this.productRepository = productRepository;
        this.em = em;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
        this.orderingDetailRepository = orderingDetailRepository;
    }

    public Long create(List<OrderCreateReqDto.OrderItemCreateReqDto> items, String principal) {
        Long memberId = Long.valueOf(principal);

        Ordering order = Ordering.builder()
                .member(em.getReference(Member.class, memberId))
                .orderStatus(OrderStatus.ORDERED)
                .build();

        orderingRepository.save(order);

        for (OrderCreateReqDto.OrderItemCreateReqDto itemDto : items) {
            long qty = itemDto.getProductCount().longValue();
            Product product = productRepository.findById(itemDto.getProductId()).orElseThrow(() -> new EntityNotFoundException("product is not found"));

            if(product.getStockQuantity() < itemDto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            product.decreaseStockQuantity(itemDto.getProductCount());
            OrderingDetails orderingDetails = OrderingDetails.builder()
                    .product(product)
                    .quantity(qty)
                    .ordering(order)
                    .build();
            orderingDetailRepository.save(orderingDetails);
        }
        return order.getId();
    }

    @Transactional(readOnly = true)
    public List<OrderListResDto> findAll(Pageable pageable) {

        Page<Long> idPage = orderingRepository.findIds(pageable);
        List<Long> ids = idPage.getContent();
        if (ids.isEmpty()) return List.of();
        List<Ordering> orders = orderingRepository.findAllByIdInWithMemberItemsProduct(ids);
        Map<Long, Ordering> map = orders.stream()
                .collect(Collectors.toMap(Ordering::getId, o -> o));
        return ids.stream().map(id -> {
            Ordering o = map.get(id);
            List<OrderListResDto.OrderDetailResDto> details =
                    o.getOrderItems().stream()
                            .map(oi -> OrderListResDto.OrderDetailResDto.builder()
                                    .detailId(oi.getId())
                                    .productName(oi.getProduct().getName())
                                    .productCount(oi.getQuantity())
                                    .build())
                            .toList();

            return OrderListResDto.builder()
                    .id(o.getId())
                    .memberEmail(o.getMember().getEmail())
                    .orderStatus(o.getOrderStatus())
                    .orderDetails(details)
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderListResDto> findMyOrders(Long memberId, Pageable pageable) {

        Page<Long> idPage = orderingRepository.findMyOrderIds(memberId, pageable);
        List<Long> ids = idPage.getContent();
        if (ids.isEmpty()) return List.of();

        List<Ordering> orders = orderingRepository.findAllByIdInWithMemberItemsProduct(ids);

        Map<Long, Ordering> map = orders.stream()
                .collect(Collectors.toMap(Ordering::getId, o -> o));

        return ids.stream().map(id -> {
            Ordering o = map.get(id);
            List<OrderListResDto.OrderDetailResDto> details =
                    o.getOrderItems().stream()
                            .map(oi -> OrderListResDto.OrderDetailResDto.builder()
                                    .detailId(oi.getId())
                                    .productName(oi.getProduct().getName())
                                    .productCount(oi.getQuantity())
                                    .build())
                            .toList();

            return OrderListResDto.builder()
                    .id(o.getId())
                    .memberEmail(o.getMember().getEmail())
                    .orderStatus(o.getOrderStatus())
                    .orderDetails(details)
                    .build();
        }).toList();
    }

}

