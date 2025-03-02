package BookingService.BookingService.controller;


import BookingService.BookingService.dto.request.ApiResponse;
import BookingService.BookingService.dto.request.UserCreationRequest;
import BookingService.BookingService.dto.request.UserUpdateRequest;
import BookingService.BookingService.dto.response.UserResponse;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.enums.Role;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;

import BookingService.BookingService.mapper.UserMapper;
import BookingService.BookingService.repository.UserRepository;
import BookingService.BookingService.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;



@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
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

    // GET thông tin chi tiết user
    // Nếu không phải ADMIN, user chỉ được truy xuất thông tin của chính mình
    // Hoặc, nếu người gọi là STAFF thì được phép lấy thông tin của specialist
    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable("userId") Long userId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStaff = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
        UserResponse response = userService.getUser(userId);
        // Nếu không phải ADMIN và không phải chính mình thì kiểm tra thêm:
        // Cho phép nếu người gọi là STAFF và user cần lấy có role SPECIALIST
        if (!isAdmin && !response.getEmail().equals(currentUserEmail)) {
            if (!(isStaff && response.getRole() == Role.SPECIALIST)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }
        return response;
    }

    // Cập nhật thông tin user
    @PutMapping("/{userId}")
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
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public List<UserResponse> getAllSpecialists() {
        return userService.getUsersByRole(Role.SPECIALIST);
    }

}
