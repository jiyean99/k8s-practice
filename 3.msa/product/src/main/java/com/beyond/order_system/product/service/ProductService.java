package com.beyond.order_system.product.service;

import com.beyond.order_system.product.domain.Product;
import com.beyond.order_system.product.dto.request.ProductCreateReqDto;
import com.beyond.order_system.product.dto.request.ProductSearchReqDto;
import com.beyond.order_system.product.dto.request.ProductStockDecreaseReqDto;
import com.beyond.order_system.product.dto.request.ProductUpdateReqDto;
import com.beyond.order_system.product.dto.response.ProductDetailResDto;
import com.beyond.order_system.product.dto.response.ProductResDto;
import com.beyond.order_system.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final EntityManager em;
    private final S3Client s3Client;
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${aws.s3.bucket}")
    private String bucket;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          EntityManager em, S3Client s3Client,
                          @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate) {
        this.productRepository = productRepository;
        this.em = em;
        this.s3Client = s3Client;
        this.redisTemplate = redisTemplate;
    }

    public ProductDetailResDto create(ProductCreateReqDto dto, String email) {

        Product product = dto.toEntity(email);

        Product saved = productRepository.save(product);

//        if (dto.getProductImage() != null && !dto.getProductImage().isEmpty()) {
//            S3UploadResult uploaded = s3Service.upload(dto.getProductImage(), "products/" + saved.getId());
//            saved.updateImagePath(uploaded.getUrl());
//        }

        if (dto.getProductImage() != null) {
            // 파일 업로드를 위한 저장객체 구성 : s3Client.putObject(저장객체, 이미지)
            String fileName = "product-" + product.getId() + "-profileimage-" + dto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName) // 파일명
                    .contentType(dto.getProductImage().getContentType()) // images/jpeg, video/mp4 등의 컨텐츠 타입 정보
                    .build();
            /// (1) AWS에 이미지 업로드(byte 형태로 변환해서 업로드)
            try {
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            /// (2) AWS의 이미지 URL 추출
            String imgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();

            // 외부와의 통신이기때문에 영속성컨텍스트가 실행되지 않을 수 있다는 우려가 있어 코드의 위치를 하단으로 옯기고,
            // 이에 따라 프로필이미지 데이터를 별도로 업데이트 처리 수행
            product.updateImagePath(imgUrl);
        }

        // 동시성 문제 해결을 위해 상품등록 시 redis에 재고 세팅
        redisTemplate.opsForValue().set(String.valueOf(product.getId()), String.valueOf(product.getStockQuantity()));

        return ProductDetailResDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ProductDetailResDto findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        return ProductDetailResDto.fromEntity(product);
    }

    //    @Transactional(readOnly = true)
//    public ProductListResDto findAll(Pageable pageable) {
//        Page<Product> page = productRepository.findAll(pageable);
//
//        return ProductListResDto.builder()
//                .content(page.getContent().stream()
//                        .map(ProductListResDto.ProductListItem::fromEntity)
//                        .toList())
//                .page(page.getNumber())
//                .size(page.getSize())
//                .totalElements(page.getTotalElements())
//                .totalPages(page.getTotalPages())
//                .build();
//    }
    @Transactional(readOnly = true)
    public Page<ProductResDto> findAll(Pageable pageable, ProductSearchReqDto searchDto) {
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if (searchDto.getProductName() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%" + searchDto.getProductName() + "%"));
                }
                if (searchDto.getCategory() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("category"), searchDto.getCategory()));
                }
                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for (int i = 0; i < predicateArr.length; i++) {
                    predicateArr[i] = predicateList.get(i);
                }
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Product> postList = productRepository.findAll(specification, pageable);
        return postList.map(p -> ProductResDto.fromEntity(p));
    }

    public void decreaseStock(ProductStockDecreaseReqDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품 없음"));
        if (product.getStockQuantity() < dto.getProductCount()) {
            throw new IllegalArgumentException("상품재고가 부족합니다.");
        } else {
            product.decreaseStockQuantity(dto.getProductCount());
        }
    }

    public void updateById(Long id, ProductUpdateReqDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
        product.updateProduct(dto);


        if (dto.getProductImage() != null) {
            // 기존 이미지 삭제
            if (product.getImagePath() != null) {
                String fileUrl = product.getImagePath();
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));
            }

            // 신규 이미지 등록
            String newFileName = "product-" + product.getId() + "-profileimage-" + dto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(newFileName) // 파일명
                    .contentType(dto.getProductImage().getContentType())
                    .build();
            try {
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String imgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(newFileName)).toExternalForm();

            product.updateImagePath(imgUrl);
        } else { // 이미지를 삭제하는 경우
            // 기존 이미지 삭제
            if (product.getImagePath() != null) {
                String fileUrl = product.getImagePath();
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));
            }
        }
    }
}
