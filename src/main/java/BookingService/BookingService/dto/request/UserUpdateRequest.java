package BookingService.BookingService.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String password;
    String currentPassword;    // Thêm field để kiểm tra mật khẩu hiện tại
    String confirmPassword;    // Thêm field để xác nhận mật khẩu mới
    String name;
    String phone;
    String address;
}
