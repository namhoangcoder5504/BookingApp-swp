package BookingService.BookingService.dto.response;


import BookingService.BookingService.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
