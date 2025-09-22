package org.example.elearningbe.payment.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.elearningbe.common.PageResponse;
import org.example.elearningbe.common.respone.ResponseData;
import org.example.elearningbe.payment.order.dto.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "Operations related to orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Checkout cart → create order (mock payment success)")
    public ResponseData<OrderResponse> checkout() {
        return new ResponseData<>(HttpStatus.OK.value(), "Checkout success", orderService.checkout());
    }

    @GetMapping
    @Operation(summary = "Get all orders of current user")
    public ResponseData<PageResponse<List<OrderResponse>>> getOrders(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return new ResponseData<>(HttpStatus.OK.value(), "Orders list", orderService.getOrders(pageNo, pageSize));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details")
    public ResponseData<OrderResponse> getOrder(@PathVariable Long orderId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Order details", orderService.getOrder(orderId));
    }
}

