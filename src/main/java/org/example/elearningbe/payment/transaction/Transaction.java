package org.example.elearningbe.payment.transaction;

import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.common.BaseEntity;
import org.example.elearningbe.common.enumerate.PaymentMethod;
import org.example.elearningbe.common.enumerate.PaymentStatus;
import org.example.elearningbe.common.enumerate.RefundStatus;
import org.example.elearningbe.payment.order.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;   // dùng BigDecimal để tránh sai số

    @Column(name = "app_trans_id", length = 100, unique = true, nullable = false)
    private String appTransId;   // Mã giao dịch merchant sinh ra (yyMMdd_orderId)

    @Column(name = "zp_trans_id", length = 100)
    private String zpTransId;    // Mã giao dịch do ZaloPay trả về

    @Column(name = "refund_id", length = 100)
    private String refundId;     // Mã refund do merchant sinh

    @Column(name = "zp_refund_id", length = 100)
    private String zpRefundId;   // Mã refund do ZaloPay trả về (nếu có)

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus; // PENDING, SUCCESS, FAILED

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false, length = 30)
    private RefundStatus refundStatus;   // NONE, PROCESSING, COMPLETED, FAILED

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod; // ZALOPAY, VNPAY, PAYPAL, COD...

    @Column(name = "payment_date")
    private LocalDateTime paymentDate; // Thời điểm giao dịch được confirm thành công
}
