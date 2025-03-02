package BookingService.BookingService.dto.request;

import lombok.Data;


@Data
public class FeedbackRequest {
    private Long bookingId;
    private int rating;
    private String comment;
}
