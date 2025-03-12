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

    private String customerName; // Tên khách hàng (bắt buộc cho guest)
    private String customerEmail; // Email khách hàng (bắt buộc cho guest)
    private String customerPhone; // Số điện thoại (tùy chọn)
}
