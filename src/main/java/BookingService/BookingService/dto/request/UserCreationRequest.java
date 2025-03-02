package BookingService.BookingService.dto.request;

import BookingService.BookingService.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {

    @Email(message = "INVALID_EMAIL")
    String email;
    @Size(min = 4, message = "PASSWORD_INVALID")
    String password;
    @NotBlank(message = "NAME_INVALID")
    String name;
    String phone;
    String address;

}
