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
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;
    UserService userService;
    @GetMapping("/google/success")
    public ApiResponse<AuthenticationResponse> googleLoginSuccess(OAuth2AuthenticationToken token, HttpServletResponse response) {
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

        // URL chuyển hướng mặc định sau khi login thành công
        String redirectUrl = "http://localhost:5174"; // Trang chính của frontend

        try {
            // Thêm token vào URL
            String redirectWithToken = redirectUrl + "?token=" + jwt;
            response.sendRedirect(redirectWithToken); // Chuyển hướng đến URL
            return null; // Không trả về JSON khi đã redirect
        } catch (IOException e) {
            throw new RuntimeException("Redirect failed after Google login", e);
        }
    }

    @GetMapping("/google/failure")
    public ApiResponse<String> googleLoginFailure(HttpServletResponse response) {
        String redirectUrl = "http://localhost:5174/login"; // URL khi thất bại

        try {
            response.sendRedirect(redirectUrl); // Chuyển hướng đến trang login
            return null; // Không trả về JSON khi đã redirect
        } catch (IOException e) {
            throw new RuntimeException("Redirect failed after Google login failure", e);
        }
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