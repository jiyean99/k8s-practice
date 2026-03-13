package com.beyond.order_system.product.dto.request;

import com.beyond.order_system.product.domain.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductCreateReqDto {
    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    @NotNull(message = "가격은 필수입니다.")
    private Double price;

    private String category;

    @NotNull(message = "재고는 필수입니다.")
    private Long stockQuantity;

    private MultipartFile productImage;

    public Product toEntity(String memberEmail) {
        return Product.builder()
                .memberEmail(memberEmail)
                .name(this.name)
                .price(this.price)
                .category(this.category)
                .stockQuantity(this.stockQuantity)
                .createdTime(LocalDateTime.now())
                .build();
    }
}
