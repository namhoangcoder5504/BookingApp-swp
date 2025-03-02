package BookingService.BookingService.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
    private Long imageId;
    private String url;
    private LocalDateTime createdAt;
    private Long serviceId;
}
