package BookingService.BookingService.dto.request;

import lombok.Data;

@Data
public class UpdateSpecialistStatusRequest {
    private String status; // Ví dụ: "ACTIVE", "INACTIVE"
}
