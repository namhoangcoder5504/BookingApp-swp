package BookingService.BookingService.repository;

import BookingService.BookingService.entity.Notification;
import BookingService.BookingService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndIsReadFalse(User user); // Lấy thông báo chưa đọc
    List<Notification> findByUser(User user); // Lấy tất cả thông báo của user
}