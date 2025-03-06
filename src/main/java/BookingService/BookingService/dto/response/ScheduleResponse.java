package BookingService.BookingService.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleResponse {
    private Long scheduleId;
    private Long specialistId;
    private String specialistName;
    private LocalDate date;
    private String timeSlot;
    private Boolean availability;
}