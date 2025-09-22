package org.example.elearningbe.payment.order;

import org.example.elearningbe.common.enumerate.OrderStatus;
import org.example.elearningbe.course.entities.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi.course FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.buyer.id = :userId AND o.status = :status")
    Page<Course> findPurchasedCourses(Long userId, OrderStatus status, Pageable pageable);

}