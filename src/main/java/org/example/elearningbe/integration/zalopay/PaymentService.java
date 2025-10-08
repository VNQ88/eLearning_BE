package org.example.elearningbe.integration.zalopay;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.common.enumerate.*;
import org.example.elearningbe.course_tracking.course_enrollment.CourseEnrollment;
import org.example.elearningbe.course_tracking.course_enrollment.CourseEnrollmentRepository;
import org.example.elearningbe.payment.order.Order;
import org.example.elearningbe.payment.order.OrderRepository;
import org.example.elearningbe.payment.transaction.Transaction;
import org.example.elearningbe.payment.transaction.TransactionRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@EnableConfigurationProperties(ZaloPayProps.class)
@RequiredArgsConstructor
public class PaymentService {
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final ZaloPayProps zaloPayProps;
    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    /** Tạo giao dịch ZaloPay cho Order */
    public Map<String, Object> createZaloPayOrder(Order order) throws Exception {
        long appTime = System.currentTimeMillis();
        String appTransId = new SimpleDateFormat("yyMMdd").format(new Date()) + "_" + order.getId();
        long amount = order.getTotalAmount().longValue();
        String embedData = "{\"redirecturl\":\"http://localhost:3000/orders\"}";
        String item = "[{\"itemid\":\"order\",\"itemname\":\"Order #" + order.getId() +
                "\",\"itemprice\":" + amount + "}]";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("app_id", zaloPayProps.getAppId());
        params.put("app_trans_id", appTransId);
        params.put("app_time", String.valueOf(appTime));
        params.put("app_user", "user_" + order.getBuyer().getId());
        params.put("amount", String.valueOf(amount));
        params.put("description", "Thanh toan don hang #" + order.getId());
        params.put("bank_code", "zalopayapp");
        params.put("callback_url", zaloPayProps.getCallbackUrl());
        params.put("embed_data", embedData);
        params.put("item", item);

        String data = zaloPayProps.getAppId() + "|" + appTransId + "|"
                + "user_" + order.getBuyer().getId() + "|" + amount + "|"
                + appTime + "|" + embedData + "|" + item;

        String mac = hmacSHA256(zaloPayProps.getKey1(), data);
        params.put("mac", mac);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);

        ResponseEntity<Map> response = restTemplate.exchange(
                zaloPayProps.getEndpoint(),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map<String, Object> result = response.getBody();
        log.info("Create Order Request: {}", params);


        Transaction tx = Transaction.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .appTransId(appTransId)
                .paymentStatus(PaymentStatus.PENDING)
                .refundStatus(RefundStatus.NONE)
                .paymentMethod(PaymentMethod.ZALO_PAY)
                .build();
        transactionRepository.save(tx);
        assert result != null;
        result.put("app_trans_id", appTransId);
        log.info("ZaloPay Response: {}", result);
        return result;
    }

    /** Callback từ ZaloPay → verify MAC + query lại để confirm */
    public Map<String, Object> handleCallback(Map<String, Object> body) throws Exception {
        log.info("Received Callback Body: {}", body);

        String data = (String) body.get("data");
        String reqMac = (String) body.get("mac");

        String myMac = hmacSHA256(zaloPayProps.getKey2(), data);
        if (!myMac.equalsIgnoreCase(reqMac)) {
            return Map.of("return_code", -1, "return_message", "mac not equal");
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> dataMap = mapper.readValue(data, Map.class);
        String appTransId = (String) dataMap.get("app_trans_id");

        Map<String, Object> queryResult = queryZaloPayOrder(appTransId);

        return Map.of("return_code", 1, "return_message", "success", "zalopay_result", queryResult);
    }

    /** Query trạng thái giao dịch */
    @Transactional
    public Map<String, Object> queryZaloPayOrder(String appTransId) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("app_id", zaloPayProps.getAppId());
        params.put("app_trans_id", appTransId);

        String data = zaloPayProps.getAppId() + "|" + appTransId + "|" + zaloPayProps.getKey1();
        String mac = hmacSHA256(zaloPayProps.getKey1(), data);
        params.put("mac", mac);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://sb-openapi.zalopay.vn/v2/query",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map<String, Object> result = response.getBody();
        log.info("Query Result: {}", result);

        transactionRepository.findByAppTransId(appTransId).ifPresent(tx -> {
            assert result != null;
            int returnCode = ((Number) result.get("return_code")).intValue();
            if (tx.getPaymentStatus() == PaymentStatus.PENDING) {
                if (returnCode == 1) {
                    // ✅ Thanh toán thành công
                    tx.setPaymentStatus(PaymentStatus.SUCCESS);
                    tx.setZpTransId(String.valueOf(result.get("zp_trans_id")));

                    Object serverTimeObj = result.get("server_time");
                    if (serverTimeObj != null) {
                        long serverTimeMillis = Long.parseLong(serverTimeObj.toString());
                        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
                        LocalDateTime paymentDateVN = Instant.ofEpochMilli(serverTimeMillis)
                                .atZone(vietnamZone).toLocalDateTime();
                        tx.setPaymentDate(paymentDateVN);
                    }

                    // ✅ Cập nhật Order sang PAID
                    Order order = tx.getOrder();
                    order.setStatus(OrderStatus.PAID);
                    orderRepository.save(order);

                    // ✅ Tạo CourseEnrollment cho mỗi OrderItem
                    order.getItems().forEach(item -> {
                        boolean exists = courseEnrollmentRepository
                                .findByUserAndCourse(order.getBuyer(), item.getCourse())
                                .isPresent();
                        if (!exists) {
                            CourseEnrollment enrollment = CourseEnrollment.builder()
                                    .user(order.getBuyer())
                                    .course(item.getCourse())
                                    .status(EnrollmentStatus.ENROLLED)
                                    .progressPercent(0.0)
                                    .startedAt(null)
                                    .completedAt(null)
                                    .build();
                            courseEnrollmentRepository.save(enrollment);
                            log.info("Created enrollment for user={} course={}",
                                    order.getBuyer().getEmail(),
                                    item.getCourse().getTitle());
                        }
                    });

                } else if (returnCode == 2) {
                    // ❌ Thanh toán thất bại
                    tx.setPaymentStatus(PaymentStatus.FAILED);
                    Order order = tx.getOrder();
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                }
                transactionRepository.save(tx);
            }
        });

        return result;
    }


    /** Hoàn tiền giao dịch */
    public Map<String, Object> refundOrder(String appTransId) throws Exception {
        Transaction tx = transactionRepository.findByAppTransId(appTransId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch trong DB"));

        if (tx.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Không thể hoàn tiền vì giao dịch chưa thành công");
        }

        if (tx.getRefundStatus() == RefundStatus.PROCESSING) {
            throw new IllegalStateException("Refund đang được xử lý");
        }
        if (tx.getRefundStatus() == RefundStatus.COMPLETED) {
            throw new IllegalStateException("Refund đã hoàn tất");
        }

        long amount = tx.getAmount().longValue();
        String zpTransId = tx.getZpTransId();

        long timestamp = System.currentTimeMillis();
        String uid = timestamp + "" + (111 + new Random().nextInt(888));
        String refundId = new SimpleDateFormat("yyMMdd").format(new Date())
                + "_" + zaloPayProps.getAppId()
                + "_" + uid;

        String refundDescription = "Hoan tien don hang";

        String data = zaloPayProps.getAppId() + "|" + zpTransId + "|" + amount + "|" + refundDescription + "|" + timestamp;
        String mac = hmacSHA256(zaloPayProps.getKey1(), data);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("app_id", zaloPayProps.getAppId());
        params.put("zp_trans_id", zpTransId);
        params.put("m_refund_id", refundId);
        params.put("amount", String.valueOf(amount));
        params.put("description", refundDescription);
        params.put("timestamp", String.valueOf(timestamp));
        params.put("mac", mac);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://sb-openapi.zalopay.vn/v2/refund",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map<String, Object> result = response.getBody();
        log.info("Refund Result: {}", result);

        assert result != null;
        int refundCode = ((Number) result.get("return_code")).intValue();
        String zpRefundId = String.valueOf(result.get("refund_id"));

        switch (refundCode) {
            case 1 -> {
                tx.setRefundStatus(RefundStatus.COMPLETED);
                tx.setRefundId(refundId);
                tx.setZpRefundId(zpRefundId);
                tx.getOrder().setStatus(org.example.elearningbe.common.enumerate.OrderStatus.CANCELLED);
                orderRepository.save(tx.getOrder());
            }
            case 2 -> tx.setRefundStatus(RefundStatus.FAILED);
            case 3 -> {
                tx.setRefundStatus(RefundStatus.PROCESSING);
                tx.setRefundId(refundId);
                tx.setZpRefundId(zpRefundId);
            }
        }
        transactionRepository.save(tx);

        return result;
    }

    /** Query trạng thái hoàn tiền */
    public Map<String, Object> queryRefundOrder(String refundId) throws Exception {
        long timestamp = System.currentTimeMillis();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("app_id", zaloPayProps.getAppId());
        params.put("m_refund_id", refundId);
        params.put("timestamp", String.valueOf(timestamp));

        String data = zaloPayProps.getAppId() + "|" + refundId + "|" + timestamp;
        String mac = hmacSHA256(zaloPayProps.getKey1(), data);
        params.put("mac", mac);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://sb-openapi.zalopay.vn/v2/query_refund",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map<String, Object> result = response.getBody();
        log.info("Refund Query Result: {}", result);

        if (result != null) {
            int returnCode = ((Number) result.get("return_code")).intValue();
            transactionRepository.findByRefundId(refundId).ifPresent(tx -> {
                switch (returnCode) {
                    case 1 -> {
                        tx.setRefundStatus(RefundStatus.COMPLETED);
                        tx.getOrder().setStatus(OrderStatus.CANCELLED);
                        orderRepository.save(tx.getOrder());
                    }
                    case 2 -> tx.setRefundStatus(RefundStatus.FAILED);
                    case 3 -> tx.setRefundStatus(RefundStatus.PROCESSING);
                }
                transactionRepository.save(tx);
            });
        }

        return result;
    }

    private String hmacSHA256(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKey);
        byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
