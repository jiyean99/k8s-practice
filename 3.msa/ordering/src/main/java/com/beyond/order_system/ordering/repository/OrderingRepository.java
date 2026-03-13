package com.beyond.order_system.ordering.repository;

import com.beyond.order_system.ordering.domain.Ordering;
import com.beyond.order_system.ordering.dto.response.OrderListResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface OrderingRepository extends JpaRepository<Ordering, Long> {
    @Query("SELECT o.id FROM Ordering o ORDER BY o.createdTime DESC")
    Page<Long> findIds(Pageable pageable);

    @Query("SELECT o FROM Ordering o " +
            "LEFT JOIN FETCH o.orderItems od " +
            "WHERE o.id IN :ids " +
            "ORDER BY o.createdTime DESC")
    List<Ordering> findAllByIdInWithMemberItemsProduct(@Param("ids") List<Long> ids);

    Page<Ordering> findByMemberEmailOrderByCreatedTimeDescIdDesc(
            String memberEmail,
            Pageable pageable
    );
}
