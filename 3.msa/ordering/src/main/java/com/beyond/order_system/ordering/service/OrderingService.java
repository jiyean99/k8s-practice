package com.beyond.order_system.ordering.service;

import com.beyond.order_system.ordering.domain.OrderStatus;
import com.beyond.order_system.ordering.domain.Ordering;
import com.beyond.order_system.ordering.dto.request.OrderCreateReqDto;
import com.beyond.order_system.ordering.dto.request.OrderItemCreateReqDto;
import com.beyond.order_system.ordering.dto.response.ProductResDto;
import com.beyond.order_system.ordering.dto.response.MyOrdersResDto;
import com.beyond.order_system.ordering.dto.response.OrderListResDto;
import com.beyond.order_system.ordering.feignClients.ProductFeignClient;
import com.beyond.order_system.ordering.repository.OrderingDetailRepository;
import com.beyond.order_system.ordering.repository.OrderingRepository;
import com.beyond.order_system.ordering.domain.OrderingDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class OrderingService {
    /* *********************** DI 주입 *********************** */
    private final OrderingRepository orderingRepository;
    private final OrderingDetailRepository orderingDetailRepository;
    private final RestTemplate restTemplate;
    private final ProductFeignClient productFeignClient;
    private final KafkaTemplate kafkaTemplate;

    @Autowired
    public OrderingService(OrderingRepository orderingRepository,
                           OrderingDetailRepository orderingDetailRepository, RestTemplate restTemplate, ProductFeignClient productFeignClient, KafkaTemplate kafkaTemplate) {
        this.orderingRepository = orderingRepository;
        this.orderingDetailRepository = orderingDetailRepository;
        this.restTemplate = restTemplate;
        this.productFeignClient = productFeignClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Long create(List<OrderItemCreateReqDto> items, String email) {
        Ordering order = Ordering.builder()
                .memberEmail(email)
                .orderStatus(OrderStatus.ORDERED)
                .build();

        orderingRepository.save(order);

        for (OrderItemCreateReqDto itemDto : items) {
            // (1) 재고 조회 요청 (동기요청: HTTP요청)
            // api gateway를 통한 호출 : http://localhost:8080/product-service/..
            // eureka에게 질의 후 product-service 직접 호출출 : http://product-service/ ..
            String productStockEndPoint = "http://product-service/product/detail/" + itemDto.getProductId();
            // HttpEntity : header + body
            HttpHeaders productStockHeaders = new HttpHeaders();
            HttpEntity<String> productStockHttpEntity = new HttpEntity<>(productStockHeaders);
            ResponseEntity<ProductResDto> responseEntity = restTemplate.exchange(productStockEndPoint, HttpMethod.GET, productStockHttpEntity, ProductResDto.class);

            ProductResDto product = responseEntity.getBody();
            if (product.getStockQuantity() < itemDto.getProductCount()) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            // (2) 주문 발생
            OrderingDetails orderingDetails = OrderingDetails.builder()
                    .ordering(order)
                    .productId(itemDto.getProductId())
                    .productName(product.getName())
                    .quantity(itemDto.getProductCount())
                    .build();
            orderingDetailRepository.save(orderingDetails);

            // (3) 재고 감소 요청 (동기/비동기요청 모두 가능, 동기: HTTP요청 기반, 비동기: 이벤트 메시지 기반)
            String productStockDecreaseEndPoint = "http://product-service/product/decrease-stock";
            HttpHeaders productStockDecreaseHeaders = new HttpHeaders();
            productStockDecreaseHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OrderItemCreateReqDto> productStockDecreaseHttpEntity = new HttpEntity<>(itemDto, productStockDecreaseHeaders);
            // 이 때 재고 감소 요청 중 에러가 발생하면 전체 롤백이 될것이다
            restTemplate.exchange(productStockDecreaseEndPoint, HttpMethod.PUT, productStockDecreaseHttpEntity, Void.class);
        }

        return order.getId();
    }

    // restTemplate -> openfeign client 개선
    public Long createFeign(List<OrderItemCreateReqDto> items, String email) {
        Ordering order = Ordering.builder()
                .memberEmail(email)
                .orderStatus(OrderStatus.ORDERED)
                .build();

        orderingRepository.save(order);

        for (OrderItemCreateReqDto itemDto : items) {
            // (1) 재고 조회 요청 (동기요청: HTTP요청)
            ProductResDto product = productFeignClient.getProductById(itemDto.getProductId());

            if (product.getStockQuantity() < itemDto.getProductCount()) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            // (2) 주문 발생
            OrderingDetails orderingDetails = OrderingDetails.builder()
                    .ordering(order)
                    .productId(itemDto.getProductId())
                    .productName(product.getName())
                    .quantity(itemDto.getProductCount())
                    .build();
            orderingDetailRepository.save(orderingDetails);

            // (3) 재고 감소 요청 (동기/비동기요청 모두 가능, 동기: HTTP요청 기반, 비동기: 이벤트 메시지 기반)
            // (3-1) 동기 요청 로직
            // productFeignClient.decreaseStockQuantity(itemDto);

            // (3-2) 비동기 로직 : kafkaTemplate를 통해 메시지 발행
            kafkaTemplate.send("stock-decrease-topic", itemDto);
        }

        return order.getId();
    }

    @Transactional(readOnly = true)
    public List<OrderListResDto> findAll(Pageable pageable) {
        Page<Long> idPage = orderingRepository.findIds(pageable);
        List<Long> ids = idPage.getContent();

        if (ids.isEmpty()) {
            return List.of();
        }

        List<Ordering> orders = orderingRepository.findAllByIdInWithMemberItemsProduct(ids);

        return orders.stream()
                .map(OrderListResDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MyOrdersResDto> findMyOrders(String email, Pageable pageable) {
        Page<Ordering> page = orderingRepository.findByMemberEmailOrderByCreatedTimeDescIdDesc(email, pageable);
        return page.getContent().stream()
                .map(MyOrdersResDto::fromEntity)
                .toList();
    }

}

