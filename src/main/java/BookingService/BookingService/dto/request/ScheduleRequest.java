package BookingService.BookingService.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleRequest {
    private Long specialistId;
    private LocalDate date;
    private String timeSlot;
    private Boolean availability;
}