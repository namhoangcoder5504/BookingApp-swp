package BookingService.BookingService.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackResponse {
    private Long feedbackId;
    private Long bookingId;
    private Long customerId;
    private Long specialistId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
