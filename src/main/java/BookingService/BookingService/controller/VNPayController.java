package BookingService.BookingService.controller;

import BookingService.BookingService.dto.request.ApiResponse;
import BookingService.BookingService.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

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
        var result = vnPayService.createPayment(paymentRequest.getAmount(), paymentRequest.getOrderInfo(), baseUrl);
        return ApiResponse.<String>builder()
                .message("Create Payment Success")
                .result(result)
                .build();
    }

    @GetMapping("/payment-info")
    public ApiResponse<String> getPaymentInfo(HttpServletRequest request) {
        return vnPayService.getPaymentInfo(request);
    }
}

// DTO để nhận dữ liệu từ body JSON
@Data
class PaymentRequest {
    private int amount;
    private String orderInfo;
}