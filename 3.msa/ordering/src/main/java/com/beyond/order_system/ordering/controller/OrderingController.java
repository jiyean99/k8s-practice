package com.beyond.order_system.ordering.controller;

import com.beyond.order_system.ordering.dto.request.OrderItemCreateReqDto;
import com.beyond.order_system.ordering.dto.response.MyOrdersResDto;
import com.beyond.order_system.ordering.dto.response.OrderListResDto;
import com.beyond.order_system.ordering.service.OrderingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderingController {
    /* *********************** DI주입 *********************** */
    private final OrderingService orderingService;

    @Autowired
    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }

    /* *********************** 컨트롤러 *********************** */
    // 주문하기
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody @Valid List<OrderItemCreateReqDto> items,
                                    @RequestHeader("X-User-Email") String email) {
        // Long OrderingID = orderingService.create(items, email);
        Long OrderingID = orderingService.createFeign(items, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderingID);
    }

    // 주문 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<OrderListResDto>> list(Pageable pageable) {
        return ResponseEntity.ok(orderingService.findAll(pageable));
    }

    // 내 주문 목록 조회
    @GetMapping("/myorders")
    public ResponseEntity<List<MyOrdersResDto>> findByMe(
            @RequestHeader("X-User-Email") String email,
            Pageable pageable
    ) {
        return ResponseEntity.ok(orderingService.findMyOrders(email, pageable));
    }

}
