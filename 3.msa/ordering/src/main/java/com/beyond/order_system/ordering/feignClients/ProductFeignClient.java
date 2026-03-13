package com.beyond.order_system.ordering.feignClients;

import com.beyond.order_system.ordering.dto.request.OrderItemCreateReqDto;
import com.beyond.order_system.ordering.dto.response.ProductResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** FeignClient의 주요 장점
 *  1) 재사용성 ↑: 다른 서비스에서도 동일 인터페이스 활용 가능
 *  2) 가독성 ↑: 서비스명 + 엔드포인트 + HTTP 메서드 일목요연
 *      - product-service : 유레카에 등록된 어플리케이션 서비스명
 *      - /product/detail/{id} : 엔드포인트
 *      - GetMapping, PutMapping : HTTP 메서드
 *  3) 편의성 ↑: 자동 응답 형변환 + 예외 처리 간편화
**/
// url 부분은 k8s의 서비스명
@Profile("local")
@FeignClient(name = "product-service", url="${product.service.url:}")
public interface ProductFeignClient {
    @GetMapping("/product/detail/{id}")
    ProductResDto getProductById(@PathVariable("id") Long id);

    @PutMapping("/product/decrease-stock")
    void decreaseStockQuantity(@RequestBody OrderItemCreateReqDto dto);
}
