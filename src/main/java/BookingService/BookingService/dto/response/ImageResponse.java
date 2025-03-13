package BookingService.BookingService.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
    private Long imageId;
    Long blogId;
    private String url;
    Long userId;
    private LocalDateTime createdAt;
    private Long serviceId;

}
