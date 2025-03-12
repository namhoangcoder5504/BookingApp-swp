package BookingService.BookingService.dto.response;

import BookingService.BookingService.enums.FeedbackStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackResponse {
    private Long feedbackId;
    private Long bookingId;


    private Long customerId;
    private String customerName;
    private Long specialistId;
    private String specialistName;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private FeedbackStatus feedbackStatus;
}
