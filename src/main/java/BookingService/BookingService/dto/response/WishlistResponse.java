package BookingService.BookingService.dto.response;

import lombok.Data;

@Data
public class WishlistResponse {
    private Long wishlistId;
    private Long userId;
    private String userName; // Thêm trường này
    private Long serviceId;
    private String serviceName; // Thêm trường này
}
