package BookingService.BookingService.dto.response;


import BookingService.BookingService.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long userId;
    String email;
    String name;
    String phone;
    String address;
    Role role;
    List<ImageResponse> images;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
