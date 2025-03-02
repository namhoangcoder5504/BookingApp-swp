package BookingService.BookingService.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequest {
    @NotBlank(message = "Image URL is required")
    private String url;

    @NotNull(message = "Service ID is required")
    private Long serviceId;
}
