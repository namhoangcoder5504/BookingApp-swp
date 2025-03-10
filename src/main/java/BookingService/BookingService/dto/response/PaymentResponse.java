package BookingService.BookingService.dto.response;

import BookingService.BookingService.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long paymentId;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private PaymentStatus status;
    private String transactionId;
    private Long bookingId;
}