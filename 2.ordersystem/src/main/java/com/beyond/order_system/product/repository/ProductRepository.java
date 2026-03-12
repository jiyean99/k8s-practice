package com.beyond.order_system.product.repository;

import com.beyond.order_system.product.domain.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAll(Specification<Product> specification, Pageable pageable);

    // [동시성 제어 방법(2)] : select for update를 통한 배타락 설정
    // - PESSIMISTIC_WRITE : 비관적락
    // - 이 때 기본 findById 메서드명으로 설정 시 모든 findById에 rock이 발생하므로 새로운 메서드명으로 작성 후 쿼리를 작성해줘야한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
