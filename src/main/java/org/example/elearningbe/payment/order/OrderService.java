package org.example.elearningbe.payment.order;

import lombok.RequiredArgsConstructor;
import org.example.elearningbe.common.PageResponse;
import org.example.elearningbe.common.enumerate.OrderStatus;
import org.example.elearningbe.exception.ResourceNotFoundException;
import org.example.elearningbe.payment.cart.Cart;
import org.example.elearningbe.payment.cart.CartItem;
import org.example.elearningbe.payment.cart.CartRepository;
import org.example.elearningbe.payment.order.dto.OrderItemResponse;
import org.example.elearningbe.payment.order.dto.OrderResponse;
import org.example.elearningbe.user.UserRepository;
import org.example.elearningbe.user.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /** Checkout từ giỏ hàng → Order (mock thanh toán: luôn PAID) */
    @Transactional
    public OrderResponse checkout() {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserIdAndCheckedOutFalse(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart has no items");
        }

        float total = (float) cart.getItems().stream()
                .mapToDouble(i -> i.getCourse().getPrice() * i.getQuantity())
                .sum();

        Order order = Order.builder()
                .buyer(user)
                .status(OrderStatus.PAID) // mock luôn thanh toán thành công
                .totalAmount(total)
                .build();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .course(cartItem.getCourse())
                    .priceAtPurchase(cartItem.getCourse().getPrice())
                    .build();
            order.getItems().add(item);
        }

        orderRepository.save(order);

        cart.setCheckedOut(true);
        cartRepository.save(cart);

        return mapToResponse(order);
    }

    /** Lấy danh sách đơn hàng của user */
    public PageResponse<List<OrderResponse>> getOrders(int pageNo, int pageSize) {
        User user = getCurrentUser();
        Page<Order> page = orderRepository.findByBuyerId(user.getId(), PageRequest.of(pageNo, pageSize));

        List<OrderResponse> responses = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<List<OrderResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .items(responses)
                .build();
    }

    /** Chi tiết đơn hàng */
    public OrderResponse getOrder(Long orderId) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new SecurityException("You are not allowed to view this order");
        }

        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getId(),
                        i.getCourse().getId(),
                        i.getCourse().getTitle(),
                        i.getPriceAtPurchase()
                ))
                .toList();

        return new OrderResponse(order.getId(), order.getTotalAmount(), order.getStatus().name(), items);
    }
}
