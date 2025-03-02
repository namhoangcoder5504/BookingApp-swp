package BookingService.BookingService.dto.request;

import BookingService.BookingService.enums.SkinType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class ServiceEntityRequest {
    @NotBlank(message = "Service name is required")
    private String name;

    @NotBlank(message = "Service description is required")
    private String description;

    @NotNull(message = "Service price is required")
    private BigDecimal price;

    @NotNull(message = "Service duration is required")
    private Integer duration;

    private Set<SkinType> recommendedSkinTypes; // Thêm trường này
}