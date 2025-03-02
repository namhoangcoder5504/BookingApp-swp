package BookingService.BookingService.controller;


import BookingService.BookingService.dto.request.*;
import BookingService.BookingService.dto.response.AuthenticationResponse;
import BookingService.BookingService.dto.response.IntrospectResponse;
import BookingService.BookingService.dto.response.UserResponse;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.enums.Role;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.service.AuthenticationService;
import BookingService.BookingService.service.UserService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;
    UserService userService;
//    @GetMapping("/google/success")
//    public ApiResponse<AuthenticationResponse> googleLoginSuccess(OAuth2AuthenticationToken token) {
//        String email = token.getPrincipal().getAttribute("email");
//        String name = token.getPrincipal().getAttribute("name");
//
//        // Kiểm tra xem user đã tồn tại chưa
//        User user;
//        try {
//            user = userService.getUserByEmail(email);
//        } catch (AppException e) {
//            // Nếu user chưa tồn tại, tạo mới
//            UserCreationRequest request = UserCreationRequest.builder()
//                    .email(email)
//                    .name(name)
//                    .password("google_default_password") // Có thể bỏ qua password vì dùng Google login
//                    .build();
//            UserResponse newUser = userService.createUser(request);
//            user = userService.getUserById(newUser.getUserId());
//        }
//
//        // Tạo JWT cho user
//        String jwt = authenticationService.generateToken(user);
//
//        return ApiResponse.<AuthenticationResponse>builder()
//                .result(AuthenticationResponse.builder()
//                        .token(jwt)
//                        .authenticated(true)
//                        .build())
//                .build();
//    }
@GetMapping("/google/success")
public ApiResponse<AuthenticationResponse> googleLoginSuccess(OAuth2AuthenticationToken token) {
    // Lấy thông tin từ Google
    String email = token.getPrincipal().getAttribute("email");
    String name = token.getPrincipal().getAttribute("name"); // Tên đầy đủ từ Google

    // Kiểm tra xem user đã tồn tại chưa
    User user;
    try {
        user = userService.getUserByEmail(email);
        // Nếu user đã tồn tại, cập nhật name nếu cần
        if (!user.getName().equals(name)) {
            user.setName(name);
            user.setUpdatedAt(java.time.LocalDateTime.now());
            userService.updateUserEntity(user);
        }
    } catch (AppException e) {
        // Nếu user chưa tồn tại, tạo mới
        user = User.builder()
                .email(email)
                .name(name) // Lưu name từ Google
                .password("google_default_password")
                .role(Role.CUSTOMER) // Gán role mặc định
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        user = userService.saveUser(user); // Lưu user vào database
    }

    // Tạo JWT cho user
    String jwt = authenticationService.generateToken(user);

    return ApiResponse.<AuthenticationResponse>builder()
            .result(AuthenticationResponse.builder()
                    .token(jwt)
                    .authenticated(true)
                    .build())
            .build();
}
    @GetMapping("/google/failure")
    public ApiResponse<String> googleLoginFailure() {
        return ApiResponse.<String>builder()
                .result("Google login failed")
                .build();
    }
    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }
}