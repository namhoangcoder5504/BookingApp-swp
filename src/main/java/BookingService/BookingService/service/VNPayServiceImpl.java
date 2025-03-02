package BookingService.BookingService.service;

import BookingService.BookingService.configuration.VnPayConfiguration;
import BookingService.BookingService.dto.request.ApiResponse;
import BookingService.BookingService.entity.Booking;
import BookingService.BookingService.entity.Payment;
import BookingService.BookingService.enums.PaymentStatus;
import BookingService.BookingService.repository.BookingRepository;
import BookingService.BookingService.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayServiceImpl implements VNPayService {

    BookingRepository bookingRepository;
    PaymentRepository paymentRepository;

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public String createPayment(int total, String orderInfo, String urlReturn) {
        // Code hiện tại của bạn giữ nguyên
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = VnPayConfiguration.getRandomNumber(8);
        String vnp_IpAddr = "127.0.0.1";
        String vnp_TmnCode = VnPayConfiguration.vnp_TmnCode;
        String orderType = "order-type";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(total * 100));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

        urlReturn += VnPayConfiguration.vnp_ReturnURL;
        vnp_Params.put("vnp_ReturnUrl", urlReturn);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfiguration.hmacSHA512(VnPayConfiguration.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VnPayConfiguration.vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;
    }

    @Override
    public int orderReturn(HttpServletRequest request) {
        Map fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        String signValue = VnPayConfiguration.hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> getPaymentInfo(HttpServletRequest request) {
        int paymentStatus = orderReturn(request);
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String transactionId = request.getParameter("vnp_TransactionNo");
        BigDecimal amount = new BigDecimal(request.getParameter("vnp_Amount")).divide(new BigDecimal(100)); // Chia cho 100 vì VNPay nhân 100

        // Giả sử orderInfo chứa bookingId
        Long bookingId = extractBookingIdFromOrderInfo(orderInfo);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Kiểm tra xem Payment với transactionId đã tồn tại chưa
        Optional<Payment> existingPaymentByTransaction = paymentRepository.findByTransactionId(transactionId);
        if (existingPaymentByTransaction.isPresent()) {
            return ApiResponse.<String>builder()
                    .message("Payment Already Processed")
                    .result("Booking ID: " + bookingId + " payment already processed with transaction ID: " + transactionId)
                    .build();
        }

        // Kiểm tra xem Payment với bookingId đã tồn tại chưa (đảm bảo chỉ một Payment cho mỗi Booking)
        Optional<Payment> existingPaymentByBooking = paymentRepository.findByBookingId(bookingId);
        if (existingPaymentByBooking.isPresent()) {
            return ApiResponse.<String>builder()
                    .message("Payment Already Processed")
                    .result("Booking ID: " + bookingId + " already has a payment record")
                    .build();
        }

        if (paymentStatus == 1) {
            // Thanh toán thành công
            Payment payment = Payment.builder()
                    .booking(booking)
                    .amount(amount)
                    .paymentMethod("VNPAY")
                    .transactionId(transactionId)
                    .status(PaymentStatus.SUCCESS)
                    .paymentTime(LocalDateTime.now())
                    .build();

            paymentRepository.save(payment);

            // Cập nhật trạng thái thanh toán của Booking
            booking.setPaymentStatus(PaymentStatus.SUCCESS);
            booking.setPayment(payment);
            bookingRepository.save(booking);

            return ApiResponse.<String>builder()
                    .message("Payment Success")
                    .result("Booking ID: " + bookingId + " paid successfully")
                    .build();
        } else if (paymentStatus == 0) {
            // Thanh toán thất bại
            Payment payment = Payment.builder()
                    .booking(booking)
                    .amount(amount)
                    .paymentMethod("VNPAY")
                    .transactionId(transactionId)
                    .status(PaymentStatus.FAILED)
                    .paymentTime(LocalDateTime.now())
                    .build();

            paymentRepository.save(payment);

            booking.setPaymentStatus(PaymentStatus.FAILED);
            booking.setPayment(payment);
            bookingRepository.save(booking);

            return ApiResponse.<String>builder()
                    .message("Payment Failed")
                    .result("Booking ID: " + bookingId + " payment failed")
                    .build();
        } else {
            return ApiResponse.<String>builder()
                    .message("Error! Secure Hash is invalid!")
                    .result(null)
                    .build();
        }
    }
    // Phương thức hỗ trợ để trích xuất bookingId từ orderInfo
    private Long extractBookingIdFromOrderInfo(String orderInfo) {
        // Giả sử orderInfo có định dạng như "Booking-123" hoặc cần điều chỉnh theo định dạng thực tế
        try {
            String[] parts = orderInfo.split("-");
            return Long.parseLong(parts[1]);
        } catch (Exception e) {
            throw new RuntimeException("Invalid orderInfo format: " + orderInfo);
        }
    }
}