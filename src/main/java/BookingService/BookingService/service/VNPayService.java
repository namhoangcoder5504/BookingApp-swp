package BookingService.BookingService.service;

import BookingService.BookingService.dto.request.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface VNPayService {
    String createPayment(int total, String orderInfo, String urlReturn);
    int orderReturn(HttpServletRequest request);
    ApiResponse<String> getPaymentInfo(HttpServletRequest request);
//    ApiResponse<TransactionResponse> getPaymentInfo(HttpServletRequest request);
ApiResponse<String> getPaymentInfo(HttpServletRequest request, HttpServletResponse response); // Cập nhật 2 tham số
}
