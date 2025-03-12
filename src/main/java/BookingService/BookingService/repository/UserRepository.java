package BookingService.BookingService.repository;

import BookingService.BookingService.entity.User;
import BookingService.BookingService.enums.Role;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);

    @Transactional
    @Modifying
    @Query("update User u set u.password = ?2 where u.email = ?1")
    void updatePassword(String email, String password);

    List<User> findByRoleAndStatus(Role role, String status);

    // Thêm query để tìm guest user hết hạn
    @Query("SELECT u FROM User u WHERE u.role = 'GUEST' AND u.status = 'TEMPORARY' AND u.createdAt < :threshold")
    List<User> findTemporaryGuestsBefore(LocalDateTime threshold);

    // Thêm query để kiểm tra xem user có booking nào không
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.customer = :user")
    boolean hasBookings(User user);
}