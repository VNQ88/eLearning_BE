package org.example.elearningbe.payment.order;

import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.common.BaseEntity;
import org.example.elearningbe.common.enumerate.OrderStatus;
import org.example.elearningbe.user.entities.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id")
    private User buyer;

    @Builder.Default // 👈 đảm bảo khi build, items luôn là ArrayList rỗng
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_amount", nullable = false)
    private float totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderStatus status; // PENDING, PAID, CANCELLED
}
