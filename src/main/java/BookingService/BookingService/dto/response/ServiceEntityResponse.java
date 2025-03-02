package BookingService.BookingService.dto.response;

import BookingService.BookingService.enums.SkinType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ServiceEntityResponse {
    private Long serviceId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<SkinType> recommendedSkinTypes; // Thêm trường này
}