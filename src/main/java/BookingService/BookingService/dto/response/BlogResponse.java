package BookingService.BookingService.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlogResponse {
    private Long blogId;
    private String title;
    private String content;
    private UserResponse author;  // Sử dụng DTO của User
    List<ImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}