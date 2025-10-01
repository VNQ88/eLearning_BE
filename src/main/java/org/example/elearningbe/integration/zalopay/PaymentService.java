package org.example.elearningbe.integration.zalopay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.common.enumerate.PaymentMethod;
import org.example.elearningbe.common.enumerate.TransactionStatus;
import org.example.elearningbe.payment.transaction.TransactionRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.elearningbe.common.enumerate.OrderStatus;
import org.example.elearningbe.payment.order.Order;
import org.example.elearningbe.payment.order.OrderRepository;
import org.example.elearningbe.payment.transaction.Transaction;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@EnableConfigurationProperties(ZaloPayProps.class)
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ZaloPayProps zaloPayProps;
    private final RestTemplate restTemplate = new RestTemplate();
    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;

    /**
     * Tạo giao dịch ZaloPay
     */
    public Map<String, Object> createZaloPayOrder(Order order) throws Exception {
        long appTime = System.currentTimeMillis();
        String appTransId = new SimpleDateFormat("yyMMdd").format(new Date()) + "_" + order.getId();

        String embedData = "{\"redirecturl\":\"http://localhost:3000/orders\"}";
        String item = "[{\"itemid\":\"course\",\"itemname\":\"Order #" + order.getId() +
                "\",\"itemprice\":" + order.getTotalAmount() + "}]";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("app_id", zaloPayProps.getAppId());
        params.put("app_trans_id", appTransId);
        params.put("app_time", String.valueOf(appTime));
        params.put("app_user", "user_" + order.getBuyer().getId());
        params.put("amount", String.valueOf((long) order.getTotalAmount()));
        params.put("description", "Thanh toan don hang #" + order.getId());
        params.put("bank_code", "zalopayapp");
        params.put("callback_url", zaloPayProps.getCallbackUrl());
        params.put("embed_data", embedData);
        params.put("item", item);

        String data = zaloPayProps.getAppId() + "|" + appTransId + "|" +
                "user_" + order.getBuyer().getId() + "|" + (long) order.getTotalAmount() + "|" +
                appTime + "|" + embedData + "|" + item;

        String mac = hmacSHA256(zaloPayProps.getKey1(), data);
        params.put("mac", mac);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                zaloPayProps.getEndpoint(),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> result = response.getBody();


        // lưu transaction
        Transaction tx = Transaction.builder()
                .order(order)
                .amount((long) order.getTotalAmount())
                .appTransId(appTransId)
                .status(TransactionStatus.PENDING)
                .paymentMethod(PaymentMethod.ZALO_PAY)
                .build();
        transactionRepository.save(tx);

        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        return result;
    }

    /** Callback từ ZaloPay */
    public Map<String, Object> handleCallback(Map<String, Object> body) throws Exception {
        log.info("Received Callback Body: {}", body);
        String data = (String) body.get("data");
        String reqMac = (String) body.get("mac");

        String myMac = hmacSHA256(zaloPayProps.getKey2(), data);
        if (!myMac.equalsIgnoreCase(reqMac)) {
            return Map.of("return_code", -1, "return_message", "mac not equal");
        }

        ObjectMapper mapper = new ObjectMapper();
        Map dataMap = mapper.readValue(data, Map.class);

        String appTransId = (String) dataMap.get("app_trans_id");
        String zpTransId = String.valueOf(dataMap.get("zp_trans_id"));

        transactionRepository.findByAppTransId(appTransId).ifPresent(tx -> {
            tx.setStatus(TransactionStatus.SUCCESS);
            tx.setZpTransId(zpTransId);
            tx.setPaymentDate(LocalDateTime.now());
            transactionRepository.save(tx);

            Order order = tx.getOrder();
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
        });

        return Map.of("return_code", 1, "return_message", "success");
    }

    /** Truy vấn trạng thái */
    public Map<String, Object> queryZaloPayOrder(String appTransId) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("app_id", zaloPayProps.getAppId());
        params.put("app_trans_id", appTransId);

        String data = zaloPayProps.getAppId() + "|" + appTransId + "|" + zaloPayProps.getKey1();
        String mac = hmacSHA256(zaloPayProps.getKey1(), data);
        params.put("mac", mac);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://sb-openapi.zalopay.vn/v2/query",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> result = response.getBody();


        transactionRepository.findByAppTransId(appTransId).ifPresent(tx -> {
            assert result != null;
            int returnCode = ((Number) result.get("return_code")).intValue();
            if (tx.getStatus() == TransactionStatus.PENDING) {
                if (returnCode == 1) {
                    tx.setStatus(TransactionStatus.SUCCESS);
                    tx.setPaymentDate(LocalDateTime.now());
                    tx.getOrder().setStatus(OrderStatus.PAID);
                } else if (returnCode == 2) {
                    tx.setStatus(TransactionStatus.FAILED);
                    tx.setPaymentDate(LocalDateTime.now());
                    tx.getOrder().setStatus(OrderStatus.CANCELLED);
                }
                transactionRepository.save(tx);
                orderRepository.save(tx.getOrder());
            }
        });
        log.info("Received Callback Result: {}", result);
        return result;
    }

    /** Hoàn tiền giao dịch */
    public Map<String, Object> refundOrder(String appTransId) throws Exception {
        // 1. Truy vấn trạng thái giao dịch trước khi refund
        Map<String, Object> queryResult = queryZaloPayOrder(appTransId);
        int returnCode = ((Number) queryResult.get("return_code")).intValue();

        if (returnCode != 1) {
            throw new IllegalStateException("Không thể hoàn tiền vì giao dịch chưa thành công");
        }

        // 2. Lấy amount và zp_trans_id từ query
        long amount = ((Number) queryResult.get("amount")).longValue();
        String zpTransId = String.valueOf(queryResult.get("zp_trans_id"));

        // 3. Sinh m_refund_id theo format yyMMdd_appid_uid
        long timestamp = System.currentTimeMillis();
        String uid = timestamp + "" + (111 + new Random().nextInt(888));
        String refundId = new SimpleDateFormat("yyMMdd").format(new Date())
                + "_" + zaloPayProps.getAppId()
                + "_" + uid;

        String refundDescription = "Hoan tien don hang";

        // 4. Tạo MAC = appid|zptransid|amount|description|timestamp
        String data = zaloPayProps.getAppId() + "|"
                + zpTransId + "|"
                + amount + "|"
                + refundDescription + "|"
                + timestamp;

        String mac = hmacSHA256(zaloPayProps.getKey1(), data);

        // 5. Build request
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

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://sb-openapi.zalopay.vn/v2/refund",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> result = response.getBody();


        // 6. Update Transaction + Order
        transactionRepository.findByAppTransId(appTransId).ifPresent(tx -> {
            tx.setRefundId(refundId);
            tx.setStatus(TransactionStatus.REFUND_PROCESSING);
            transactionRepository.save(tx);

            Order order = tx.getOrder();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        });

        return result;
    }

    /** Kiểm tra trạng thái hoàn tiền */
    public Map<String, Object> queryRefundOrder(String refundId) throws Exception {
        long timestamp = System.currentTimeMillis();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("app_id", zaloPayProps.getAppId());
        params.put("m_refund_id", refundId);
        params.put("timestamp", String.valueOf(timestamp));

        // mac = app_id|m_refund_id|timestamp
        String data = zaloPayProps.getAppId() + "|" + refundId + "|" + timestamp;
        String mac = hmacSHA256(zaloPayProps.getKey1(), data);
        params.put("mac", mac);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://sb-openapi.zalopay.vn/v2/query_refund",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> result = response.getBody();

        //  Update Transaction nếu thành công
        if (result != null) {
            int returnCode = ((Number) result.get("return_code")).intValue();
            transactionRepository.findByRefundId(refundId).ifPresent(tx -> {
                if (returnCode == 1) {
                    tx.setStatus(TransactionStatus.REFUNDED);
                    tx.setPaymentDate(LocalDateTime.now());
                    transactionRepository.save(tx);
                }
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

