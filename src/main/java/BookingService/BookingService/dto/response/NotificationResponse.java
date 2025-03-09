package BookingService.BookingService.dto.response;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String message;
    private LocalDateTime createdAt; // Thời gian tạo thông báo
    private boolean isRead;         // Trạng thái đã đọc hay chưa
}