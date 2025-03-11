package BookingService.BookingService.dto.request;


import lombok.Data;

@Data
public class ContactRequest {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String message;
}