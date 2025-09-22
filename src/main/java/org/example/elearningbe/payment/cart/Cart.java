package org.example.elearningbe.payment.cart;

import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.common.BaseEntity;
import org.example.elearningbe.user.entities.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default // 👈 đảm bảo khi dùng builder thì items luôn khởi tạo rỗng
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Builder.Default
    @Column(name = "checked_out")
    private boolean checkedOut = false;
}
