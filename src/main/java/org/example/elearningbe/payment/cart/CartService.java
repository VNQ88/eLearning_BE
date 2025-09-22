package org.example.elearningbe.payment.cart;
import lombok.RequiredArgsConstructor;
import org.example.elearningbe.course.CourseRepository;
import org.example.elearningbe.course.entities.Course;
import org.example.elearningbe.exception.ResourceNotFoundException;
import org.example.elearningbe.payment.cart.dto.CartItemResponse;
import org.example.elearningbe.payment.cart.dto.CartResponse;
import org.example.elearningbe.user.UserRepository;
import org.example.elearningbe.user.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserIdAndCheckedOutFalse(user.getId())
                .orElseGet(() -> {
                    Cart cart = Cart.builder().user(user).checkedOut(false).build();
                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public CartResponse addToCart(Long courseId) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // check nếu đã có khóa học trong giỏ
        boolean exists = cart.getItems().stream()
                .anyMatch(i -> i.getCourse().getId().equals(courseId));
        if (exists) {
            throw new IllegalArgumentException("Course already in cart");
        }

        CartItem item = CartItem.builder()
                .cart(cart)
                .course(course)
                .quantity(1)
                .build();
        cart.getItems().add(item);

        cartRepository.save(cart);
        return mapToResponse(cart);
    }

    public CartResponse getCart() {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long itemId) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse clearCart() {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getId(),
                        i.getCourse().getId(),
                        i.getCourse().getTitle(),
                        i.getCourse().getPrice(),
                        i.getQuantity()
                ))
                .toList();

        float total = (float) items.stream()
                .mapToDouble(i -> i.price() * i.quantity())
                .sum();

        return new CartResponse(cart.getId(), items, total);
    }
}

