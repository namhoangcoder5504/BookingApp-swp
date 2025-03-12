package BookingService.BookingService.dto.response;


import BookingService.BookingService.enums.BookingStatus;
import BookingService.BookingService.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class BookingResponse {
    private Long bookingId;
    private Long customerId;
    private String customerName;
    private Long specialistId;
        private String specialistName;
    private List<String> serviceNames;
    private Map<String, Integer> serviceDurations;
    private Map<String, BigDecimal> servicePrices;
    private Integer totalDuration;
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
