package BookingService.BookingService.controller;

import BookingService.BookingService.dto.request.ApiResponse;
import BookingService.BookingService.dto.response.PaymentResponse;
import BookingService.BookingService.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vnpay")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayController {

    VNPayService vnPayService;

    @PostMapping(value = "/create-payment", produces = "application/json;charset=UTF-8", consumes = "application/json")
    public ApiResponse<String> CreatePayment(
            @RequestBody PaymentRequest paymentRequest, // Nhận dữ liệu từ body JSON
            HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        var result = vnPayService.createPayment(paymentRequest.getAmount().intValue(), paymentRequest.getOrderInfo(), baseUrl); // Chuyển BigDecimal thành int
        return ApiResponse.<String>builder()
                .message("Create Payment Success")
                .result(result)
                .build();
    }

    @GetMapping("/payment-info")
    public ApiResponse<String> getPaymentInfo(HttpServletRequest request, HttpServletResponse response) {
        return vnPayService.getPaymentInfo(request, response);
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> payments = vnPayService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
}

// DTO để nhận dữ liệu từ body JSON
@Data
class PaymentRequest {
    private BigDecimal amount; // Thay int thành BigDecimal
    private String orderInfo;
}