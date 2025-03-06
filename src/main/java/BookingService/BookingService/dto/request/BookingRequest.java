package BookingService.BookingService.dto.request;

import BookingService.BookingService.enums.BookingStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingRequest {


    private Long specialistId;

    @NotNull(message = "Booking date is required")
    private LocalDate bookingDate;
    private String startTime;

    @NotEmpty // hoặc @NotNull
    private List<Long> serviceIds;
}
