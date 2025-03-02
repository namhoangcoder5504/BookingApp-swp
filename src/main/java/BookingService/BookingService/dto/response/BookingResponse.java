package BookingService.BookingService.dto.response;


import BookingService.BookingService.enums.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Long bookingId;
    private Long customerId;
    private Long specialistId;
    private LocalDate bookingDate;
    private String timeSlot;
    private BookingStatus status;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
