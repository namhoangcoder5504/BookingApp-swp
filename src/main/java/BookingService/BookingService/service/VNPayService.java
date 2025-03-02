package BookingService.BookingService.service;

import BookingService.BookingService.dto.request.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface VNPayService {
    String createPayment(int total, String orderInfo, String urlReturn);
    int orderReturn(HttpServletRequest request);
    ApiResponse<String> getPaymentInfo(HttpServletRequest request);
//    ApiResponse<TransactionResponse> getPaymentInfo(HttpServletRequest request);
}
