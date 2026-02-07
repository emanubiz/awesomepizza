package com.awesomepizza.order.repository;

import com.awesomepizza.order.domain.entity.Order;
import com.awesomepizza.order.domain.enums.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);

    Optional<Order> findFirstByStatusOrderByCreatedAtAsc(OrderStatus status);

    boolean existsByStatus(OrderStatus status);

    List<Order> findByStatus(OrderStatus status);
}