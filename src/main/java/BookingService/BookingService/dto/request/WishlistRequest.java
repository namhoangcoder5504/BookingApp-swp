package BookingService.BookingService.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WishlistRequest {
    @NotNull(message = "Service id is required")
    private Long serviceId;
}
