package com.beyond.order_system.ordering.repository;

import com.beyond.order_system.ordering.domain.Ordering;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface OrderingRepository extends JpaRepository<Ordering, Long> {
    // 생성시간 기준으로 "주문 ID만" 페이징
    @Query("""
        select o.id
        from Ordering o
        order by o.createdTime desc, o.id desc
    """)
    Page<Long> findIds(Pageable pageable);

    // 위 id들에 대해서만 상세/상품/회원까지 한 번에 로딩
    @Query("""
        select distinct o
        from Ordering o
        join fetch o.member m
        left join fetch o.orderItems oi
        left join fetch oi.product p
        where o.id in :ids
    """)
    List<Ordering> findAllByIdInWithMemberItemsProduct(@Param("ids") List<Long> ids);

    // 내 주문: id만 페이징 (createdTime desc)
    @Query("""
        select o.id
        from Ordering o
        where o.member.id = :memberId
        order by o.createdTime desc, o.id desc
    """)
    Page<Long> findMyOrderIds(@Param("memberId") Long memberId, Pageable pageable);

}
