package com.beyond.order_system.product.service;

import com.beyond.order_system.product.dto.request.ProductStockDecreaseReqDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StockKafkaListener {
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @Autowired
    public StockKafkaListener(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
    }

    // 아래의 리스너는 토픽을 바라보고있다가 message 매개변수로 메시지를 받아오게 되는 것
    @KafkaListener(topics = "stock-decrease-topic", containerFactory = "kafkaListener")
    public void stockConsumer(String message) throws JsonProcessingException {
        ProductStockDecreaseReqDto dto = objectMapper.readValue(message, ProductStockDecreaseReqDto.class);
        productService.decreaseStock(dto);
    }
}
