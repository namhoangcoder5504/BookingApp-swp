package BookingService.BookingService.dto.response;


import BookingService.BookingService.enums.BookingStatus;
import BookingService.BookingService.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingResponse {
    private Long bookingId;
    private Long customerId;
    private Long specialistId;
    private List<String> serviceNames;
    private LocalDate bookingDate;
    private String timeSlot;
    private BookingStatus status;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PaymentStatus paymentStatus;
}
