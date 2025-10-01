package org.example.elearningbe.payment.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CheckoutResponse {
    private Long orderId;
    private Map<String, Object> zaloPayRes;

}
