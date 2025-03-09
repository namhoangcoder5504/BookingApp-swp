package BookingService.BookingService.controller;

import BookingService.BookingService.dto.response.NotificationResponse;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.service.BookingService;
import BookingService.BookingService.service.NotificationService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final BookingService bookingService;

    @GetMapping("/notifications")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','STAFF','SPECIALIST')")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        User currentUser = bookingService.getCurrentUser();
        List<NotificationResponse> notifications = notificationService.getUnreadNotificationResponses(currentUser);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/notifications/{id}/read")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','STAFF','SPECIALIST')")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable @Positive Long id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 nếu không tìm thấy
        }
    }
}