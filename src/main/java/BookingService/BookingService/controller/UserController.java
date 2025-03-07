package BookingService.BookingService.controller;


import BookingService.BookingService.dto.request.ApiResponse;
import BookingService.BookingService.dto.request.UpdateSpecialistStatusRequest;
import BookingService.BookingService.dto.request.UserCreationRequest;
import BookingService.BookingService.dto.request.UserUpdateRequest;
import BookingService.BookingService.dto.response.SpecialistResponse;
import BookingService.BookingService.dto.response.UserResponse;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.enums.Role;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;

import BookingService.BookingService.mapper.SpecialistMapper;
import BookingService.BookingService.mapper.UserMapper;
import BookingService.BookingService.repository.UserRepository;
import BookingService.BookingService.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SpecialistMapper specialistMapper;
    // Tạo user - mở public (không cần token)

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserCreationRequest request) {
        return userService.createUser(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserResponse>> getUsers() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Current user: {}", authentication.getName());
        authentication.getAuthorities().forEach(a -> log.info(a.getAuthority()));
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAllUsers())
                .build();
    }

    @PutMapping("/specialists/{specialistId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<String> updateSpecialistStatus(
            @PathVariable Long specialistId,
            @RequestBody UpdateSpecialistStatusRequest request) {
        userService.updateSpecialistStatus(specialistId, request);
        return ResponseEntity.ok("Cập nhật trạng thái chuyên viên thành công: " + request.getStatus());
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable("userId") Long userId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStaff = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
        UserResponse response = userService.getUser(userId);

        if (!isAdmin && !response.getEmail().equals(currentUserEmail)) {
            if (!(isStaff && response.getRole() == Role.SPECIALIST)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }
        return response;
    }
    @GetMapping("/profile")
    public UserResponse getUserProfile() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserProfileByEmail(currentUserEmail); // Trả về DTO thay vì entity
    }


    @PutMapping("/profile")
    public UserResponse updateProfile(@RequestBody UserUpdateRequest request) {
        return userService.updateProfileUser(request);
    }
    // Cập nhật thông tin user

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        UserResponse existing = userService.getUser(userId);
        if (!isAdmin && !existing.getEmail().equals(currentUserEmail)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return userService.updateUser(userId, request);
    }

    // Xoá user
    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable Long userId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        UserResponse existing = userService.getUser(userId);
        if (!isAdmin && !existing.getEmail().equals(currentUserEmail)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        userService.deleteUser(userId);
        return "User deleted successfully!";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/assign-role")
    public UserResponse assignRoleToUser(
            @PathVariable Long userId,
            @RequestParam Role newRole
    ) {
        // Lấy user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Gán role
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }
    // Lấy danh sách toàn bộ Specialist
    @GetMapping("/specialists")
    public List<SpecialistResponse> getAllSpecialists() {
        List<User> specialists = userRepository.findByRole(Role.SPECIALIST);
        return specialists.stream()
                .map(specialistMapper::toSpecialistResponse)
                .collect(Collectors.toList());
    }

    // Thêm endpoint hiển thị một chuyên viên theo ID
    @GetMapping("/specialists/{specialistId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public SpecialistResponse getSpecialistById(@PathVariable Long specialistId) {
        User specialist = userRepository.findById(specialistId)
                .filter(user -> user.getRole() == Role.SPECIALIST)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return specialistMapper.toSpecialistResponse(specialist);
    }

    @GetMapping("/specialists/active")

    public List<SpecialistResponse> getActiveSpecialists() {
        List<User> activeSpecialists = userRepository.findByRoleAndStatus(Role.SPECIALIST, "ACTIVE");
        return activeSpecialists.stream()
                .map(specialistMapper::toSpecialistResponse)
                .collect(Collectors.toList());
    }

}
