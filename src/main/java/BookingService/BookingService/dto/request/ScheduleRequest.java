package BookingService.BookingService.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
@Data
public class ScheduleRequest {
    @NotNull(message = "Date cannot be null")
    private LocalDate date;

    @NotNull(message = "Time slot cannot be null")
    private String timeSlot;

    @NotNull(message = "Specialist ID cannot be null")
    private Long specialistId;

    // availability có thể để mặc định là true/false hoặc tính toán phía server
    private Boolean availability;
}
