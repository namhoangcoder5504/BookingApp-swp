package BookingService.BookingService.service;

import BookingService.BookingService.entity.Booking;
import BookingService.BookingService.entity.Notification;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Lưu thông báo trên hệ thống web
    public void createWebNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    // Lấy thông báo chưa đọc cho user
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalse(user);
    }

    // Đánh dấu thông báo đã đọc
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // Thông báo cho chuyên viên khi có booking mới (email)
    public void notifySpecialistNewBooking(Booking booking) {
        String subject = "Thông báo: Bạn có lịch hẹn mới tại Beautya";
        String body = "<!DOCTYPE html><html><body>" +
                "<h2>Lịch Hẹn Mới</h2>" +
                "<p>Xin chào " + booking.getSpecialist().getName() + ",</p>" +
                "<p>Bạn có lịch hẹn mới với khách hàng <strong>" + booking.getCustomer().getName() + "</strong> " +
                "vào ngày <strong>" + booking.getBookingDate().format(DATE_FORMATTER) + "</strong>, " +
                "khung giờ <strong>" + booking.getTimeSlot() + "</strong>.</p>" +
                "<p>Vui lòng chuẩn bị để phục vụ khách hàng!</p>" +
                "</body></html>";
        emailService.sendEmail(booking.getSpecialist().getEmail(), subject, body);
    }

    // Thông báo cho chuyên viên khi lịch bị hủy (email)
    public void notifySpecialistBookingCancelled(Booking booking) {
        String subject = "Thông báo: Lịch hẹn của bạn đã bị hủy";
        String body = "<!DOCTYPE html><html><body>" +
                "<h2>Lịch Hẹn Bị Hủy</h2>" +
                "<p>Xin chào " + booking.getSpecialist().getName() + ",</p>" +
                "<p>Lịch hẹn với khách hàng <strong>" + booking.getCustomer().getName() + "</strong> " +
                "vào ngày <strong>" + booking.getBookingDate().format(DATE_FORMATTER) + "</strong>, " +
                "khung giờ <strong>" + booking.getTimeSlot() + "</strong> đã bị hủy.</p>" +
                "<p>Khung giờ này hiện đã trống, bạn có thể nhận lịch mới.</p>" +
                "</body></html>";
        emailService.sendEmail(booking.getSpecialist().getEmail(), subject, body);
    }
}