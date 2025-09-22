package org.example.elearningbe.payment.cart;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.elearningbe.common.respone.ResponseData;
import org.example.elearningbe.payment.cart.dto.CartResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Cart Controller", description = "Operations related to shopping cart")
public class CartController {
    private final CartService cartService;

    @PostMapping("/add")
    @Operation(summary = "Add course to cart", description = "Add a course to the current user's cart")
    public ResponseData<CartResponse> addToCart(@RequestParam Long courseId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Added to cart", cartService.addToCart(courseId));
    }

    @GetMapping
    @Operation(summary = "Get current cart", description = "Retrieve the current user's cart details")
    public ResponseData<CartResponse> getCart() {
        return new ResponseData<>(HttpStatus.OK.value(), "Cart details", cartService.getCart());
    }

    @DeleteMapping("/item/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Remove a specific item from the current user's cart")
    public ResponseData<CartResponse> removeItem(@PathVariable Long itemId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Item removed", cartService.removeItem(itemId));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear cart", description = "Clear all items from the current user's cart")
    public ResponseData<CartResponse> clearCart() {
        return new ResponseData<>(HttpStatus.OK.value(), "Cart cleared", cartService.clearCart());
    }
}

