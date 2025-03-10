package BookingService.BookingService.service;

import BookingService.BookingService.configuration.VnPayConfiguration;
import BookingService.BookingService.dto.request.ApiResponse;
import BookingService.BookingService.dto.response.PaymentResponse;
import BookingService.BookingService.entity.Booking;
import BookingService.BookingService.entity.Payment;
import BookingService.BookingService.enums.BookingStatus;
import BookingService.BookingService.enums.PaymentStatus;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.repository.BookingRepository;
import BookingService.BookingService.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayServiceImpl implements VNPayService {

    BookingRepository bookingRepository;
    PaymentRepository paymentRepository;

    // URL thành công mặc định, có thể thay đổi
    private static final String DEFAULT_SUCCESS_REDIRECT_URL = "http://localhost:5173/mybooking";

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public String createPayment(int total, String orderInfo, String urlReturn) {
        // [NEW] Extract bookingId and check booking status
        Long bookingId = extractBookingIdFromOrderInfo(orderInfo);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        // [NEW] Check if booking status is IN_PROGRESS
        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.BOOKING_NOT_CHECKED_IN);
        }

        // [NEW] Kiểm tra tổng tiền truyền vào khớp với totalPrice của booking
        if (total != booking.getTotalPrice().intValue()) {
            throw new AppException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        Payment existingPayment = paymentRepository.findByBookingId(bookingId)
                .orElse(null);

        if (existingPayment != null && PaymentStatus.SUCCESS.equals(existingPayment.getStatus())) {
            throw new AppException(ErrorCode.PAYMENT_NOT_COMPLETED); // Đã thanh toán thành công, không cho tạo mới
        }

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
        vnp_Params.put("vnp_Amount", String.valueOf(total * 100)); // VNPay yêu cầu số tiền nhân 100
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

        // Lưu payment mới nếu chưa tồn tại, hoặc tái sử dụng existingPayment nếu có
        if (existingPayment == null) {
            existingPayment = Payment.builder()
                    .booking(booking)
                    .amount(BigDecimal.valueOf(total))
                    .paymentMethod("VNPAY")
                    .transactionId(vnp_TxnRef) // Sử dụng transactionId mới
                    .status(PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(existingPayment);
        } else {
            existingPayment.setTransactionId(vnp_TxnRef); // Cập nhật transactionId mới cho lần thanh toán lại
            existingPayment.setStatus(PaymentStatus.PENDING); // Đặt lại trạng thái thành PENDING
            paymentRepository.save(existingPayment);
        }

        return VnPayConfiguration.vnp_PayUrl + "?" + queryUrl;
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
    public ApiResponse<String> getPaymentInfo(HttpServletRequest request) {
        return null;
    }

    @Override
    @Transactional
    public ApiResponse<String> getPaymentInfo(HttpServletRequest request, HttpServletResponse response) {
        int paymentStatus = orderReturn(request);
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String transactionId = request.getParameter("vnp_TransactionNo");
        BigDecimal amount = new BigDecimal(request.getParameter("vnp_Amount")).divide(new BigDecimal(100));
        String redirectUrlParam = request.getParameter("redirectUrl"); // Lấy redirectUrl từ request

        Long bookingId = extractBookingIdFromOrderInfo(orderInfo);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Lấy payment hiện tại của booking
        Payment existingPayment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        // Kiểm tra nếu đã có payment SUCCESS từ trước
        if (existingPayment.getStatus() == PaymentStatus.SUCCESS) {
            try {
                String redirectUrl = redirectUrlParam != null && !redirectUrlParam.isEmpty() ? redirectUrlParam : DEFAULT_SUCCESS_REDIRECT_URL;
                response.sendRedirect(redirectUrl); // Chuyển hướng ngay lập tức
                return null; // Không trả JSON khi redirect
            } catch (IOException e) {
                throw new RuntimeException("Redirect failed", e);
            }
        }

        if (paymentStatus == 1) {
            // Cập nhật payment hiện tại thành SUCCESS thay vì tạo mới
            existingPayment.setStatus(PaymentStatus.SUCCESS);
            existingPayment.setTransactionId(transactionId); // Cập nhật transactionId mới
            existingPayment.setAmount(amount);
            existingPayment.setPaymentTime(LocalDateTime.now());
            paymentRepository.save(existingPayment);

            booking.setPaymentStatus(PaymentStatus.SUCCESS);
            booking.setPayment(existingPayment);
            bookingRepository.save(booking);

            try {
                String redirectUrl = redirectUrlParam != null && !redirectUrlParam.isEmpty() ? redirectUrlParam : DEFAULT_SUCCESS_REDIRECT_URL;
                response.sendRedirect(redirectUrl); // Chuyển hướng ngay lập tức
                return null; // Không trả JSON khi redirect
            } catch (IOException e) {
                throw new RuntimeException("Redirect failed", e);
            }
        } else if (paymentStatus == 0) {
            // Cập nhật trạng thái thành FAILED nếu thanh toán thất bại
            existingPayment.setStatus(PaymentStatus.FAILED);
            existingPayment.setTransactionId(transactionId);
            existingPayment.setAmount(amount);
            existingPayment.setPaymentTime(LocalDateTime.now());
            paymentRepository.save(existingPayment);

            booking.setPaymentStatus(PaymentStatus.FAILED);
            booking.setPayment(existingPayment);
            bookingRepository.save(booking);

            return ApiResponse.<String>builder()
                    .code(1001) // Thất bại
                    .message("Payment Failed")
                    .result("Booking ID: " + bookingId + " payment failed with transaction ID: " + transactionId)
                    .build();
        } else {
            return ApiResponse.<String>builder()
                    .code(1002) // Lỗi
                    .message("Error! Secure Hash is invalid!")
                    .result(null)
                    .build();
        }
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(payment -> {
                    PaymentResponse response = new PaymentResponse();
                    response.setPaymentId(payment.getPaymentId());
                    response.setAmount(payment.getAmount());
                    response.setPaymentMethod(payment.getPaymentMethod());
                    response.setPaymentTime(payment.getPaymentTime());
                    response.setStatus(payment.getStatus());
                    response.setTransactionId(payment.getTransactionId());
                    response.setBookingId(payment.getBooking() != null ? payment.getBooking().getBookingId() : null);
                    return response;
                })
                .collect(Collectors.toList());
    }

    private Long extractBookingIdFromOrderInfo(String orderInfo) {
        try {
            String[] parts = orderInfo.split("-");
            return Long.parseLong(parts[1]);
        } catch (Exception e) {
            throw new RuntimeException("Invalid orderInfo format: " + orderInfo);
        }
    }
}