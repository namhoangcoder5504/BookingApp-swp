package BookingService.BookingService.controller;

import BookingService.BookingService.dto.request.BookingRequest;
import BookingService.BookingService.dto.response.BookingResponse;
import BookingService.BookingService.entity.Notification;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.enums.BookingStatus;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.service.BookingService;
import BookingService.BookingService.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final NotificationService notificationService;
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        Optional<BookingResponse> booking = bookingService.getBookingById(id);
        return booking.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','STAFF','SPECIALIST')")
    public ResponseEntity<List<BookingResponse>> getBookingsForCurrentUser() {
        List<BookingResponse> responses = bookingService.getBookingsForCurrentUser();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','STAFF')") // Loại SPECIALIST nếu không cần
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.cancelBookingByUser(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<BookingResponse> updateBookingStatus(@PathVariable Long id) {
        BookingResponse response = bookingService.updateBookingStatusByStaff(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/checkin")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BookingResponse> checkInBooking(@PathVariable Long bookingId) {
        BookingResponse response = bookingService.checkInBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BookingResponse> checkOutBooking(@PathVariable Long bookingId) {
        BookingResponse response = bookingService.checkOutBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.updateBooking(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/revenue/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BigDecimal> getDailyRevenue(
            @RequestParam(required = false) String date) { // Tham số dạng "yyyy-MM-dd"
        LocalDate localDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        BigDecimal revenue = bookingService.getDailyRevenue(localDate);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/revenue/weekly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BigDecimal> getWeeklyRevenue(
            @RequestParam(required = false) String dateInWeek) { // Tham số dạng "yyyy-MM-dd"
        LocalDate localDate = (dateInWeek != null) ? LocalDate.parse(dateInWeek) : LocalDate.now();
        BigDecimal revenue = bookingService.getWeeklyRevenue(localDate);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/revenue/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BigDecimal> getMonthlyRevenue(
            @RequestParam int year,
            @RequestParam int month) {
        BigDecimal revenue = bookingService.getMonthlyRevenue(year, month);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/revenue/range")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BigDecimal> getRevenueInRange(
            @RequestParam String startDate, // Tham số dạng "yyyy-MM-dd"
            @RequestParam String endDate) { // Tham số dạng "yyyy-MM-dd"
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        BigDecimal revenue = bookingService.getRevenueInRange(start, end);
        return ResponseEntity.ok(revenue);
    }
}