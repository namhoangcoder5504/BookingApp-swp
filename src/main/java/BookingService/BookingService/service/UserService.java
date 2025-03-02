package BookingService.BookingService.service;

import BookingService.BookingService.dto.request.UserCreationRequest;
import BookingService.BookingService.dto.request.UserUpdateRequest;
import BookingService.BookingService.dto.response.UserResponse;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.enums.Role;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.mapper.UserMapper;
import BookingService.BookingService.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    /**
     * Tạo user mới
     */
    public User saveUser(User user) {
        // Kiểm tra email trùng
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        return userRepository.save(user);
    }

    public User updateUserEntity(User user) {
        // Kiểm tra xem user có tồn tại không trước khi cập nhật
        if (!userRepository.existsById(user.getUserId())) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        return userRepository.save(user);
    }
    public UserResponse createUser(UserCreationRequest request) {
        // Kiểm tra email trùng
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Chuyển đổi DTO thành Entity
        User user = userMapper.toUser(request);

        // Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Gán vai trò mặc định là CUSTOMER
        user.setRole(Role.CUSTOMER);

        // Thiết lập thời gian tạo và cập nhật
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Lưu vào cơ sở dữ liệu
        userRepository.save(user);

        // Trả về đối tượng phản hồi
        return userMapper.toUserResponse(user);
    }


    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }


    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
    /**
     * Lấy thông tin user
     */
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }
    /**
     * Chỉ ADMIN mới được lấy danh sách users
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    /**
     * Cập nhật user (nếu có password mới -> encode)
     */
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Map các field từ request vào entity
        userMapper.updateUser(user, request);

        // Nếu request có password mới
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Trả về DTO
        return userMapper.toUserResponse(user);
    }

    /**
     * Xoá user
     */
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        userRepository.deleteById(userId);
    }
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .map(userMapper::toUserResponse)
                .toList();
    }



}