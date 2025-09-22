package org.example.elearningbe.payment.cart;

import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.common.BaseEntity;
import org.example.elearningbe.course.entities.Course;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Builder.Default
    @Column(nullable = false)
    private int quantity = 1; // mặc định mỗi khóa học chỉ mua 1
}
