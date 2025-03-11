package BookingService.BookingService.dto.response;

import BookingService.BookingService.enums.ContactStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContactResponse {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String message;
    private ContactStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}