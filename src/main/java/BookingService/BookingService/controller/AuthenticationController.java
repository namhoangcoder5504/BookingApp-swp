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
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;
    UserService userService;
    @GetMapping("/google/success")
    public void googleLoginSuccess(OAuth2AuthenticationToken token, HttpServletResponse response) throws IOException {
        String email = token.getPrincipal().getAttribute("email");
        String name = token.getPrincipal().getAttribute("name");

        User user;
        try {
            user = userService.getUserByEmail(email);
            if (!user.getName().equals(name)) {
                user.setName(name);
                user.setUpdatedAt(java.time.LocalDateTime.now());
                userService.updateUserEntity(user);
            }
        } catch (AppException e) {
            user = User.builder()
                    .email(email)
                    .name(name)
                    .password(UUID.randomUUID().toString()) // Mật khẩu ngẫu nhiên
                    .role(Role.CUSTOMER)
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();
            user = userService.saveUser(user);
        }

        String jwt = authenticationService.generateToken(user);
        // Redirect về frontend với token
        String redirectUrl = "http://localhost:5173/login?token=" + jwt;
        response.sendRedirect(redirectUrl);
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