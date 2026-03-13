package com.beyond.order_system.product.controller;

import com.beyond.order_system.product.dto.request.ProductCreateReqDto;
import com.beyond.order_system.product.dto.request.ProductSearchReqDto;
import com.beyond.order_system.product.dto.request.ProductStockDecreaseReqDto;
import com.beyond.order_system.product.dto.request.ProductUpdateReqDto;
import com.beyond.order_system.product.dto.response.ProductDetailResDto;
import com.beyond.order_system.product.dto.response.ProductResDto;
import com.beyond.order_system.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/product")
public class ProductController {
    /* *********************** DI 주입 *********************** */
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /* *********************** 컨트롤러 *********************** */
    // 상품 등록
    @PostMapping("/create")
    public ResponseEntity<ProductDetailResDto> create(
            @ModelAttribute @Valid ProductCreateReqDto dto,
            @RequestHeader("X-User-Email") String email
    ) {
        ProductDetailResDto res = productService.create(dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // 상품 상세 조회
    @GetMapping("/detail/{id}")
    public ProductDetailResDto findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping("/list")
    public ResponseEntity<?> findAll(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable, ProductSearchReqDto searchDto) {
        Page<ProductResDto> productResDtoList = productService.findAll(pageable, searchDto);
        return ResponseEntity.status(HttpStatus.OK).body(productResDtoList);

    }

    @PutMapping("/decrease-stock")
    public ResponseEntity<?> decreaseStock(@RequestBody ProductStockDecreaseReqDto dto) {
        productService.decreaseStock(dto);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateById(@PathVariable Long id, @ModelAttribute @Valid ProductUpdateReqDto dto) {
        productService.updateById(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }
}
