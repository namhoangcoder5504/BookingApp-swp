package BookingService.BookingService.controller;
import BookingService.BookingService.dto.request.BookingRequest;
import BookingService.BookingService.dto.response.BookingResponse;
import BookingService.BookingService.enums.BookingStatus;
import BookingService.BookingService.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        Optional<BookingResponse> booking = bookingService.getBookingById(id);
        return booking.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequest request
    ) {
        return ResponseEntity.ok(bookingService.updateBooking(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    /**
     *  Chỉ ROLE_CUSTOMER, ROLE_ADMIN, ROLE_STAFF, ROLE_SPECIALIST
     *  mới gọi được endpoint này (đã config @PreAuthorize).
     *  Không cần truyền email, Service sẽ tự lấy từ SecurityContextHolder.
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','STAFF','SPECIALIST')")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.cancelBookingByUser(id);
        return ResponseEntity.ok(response);
    }

    /**
     *  Chỉ ROLE_STAFF hoặc ROLE_ADMIN được cập nhật trạng thái
     */
    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus newStatus
    ) {
        BookingResponse response = bookingService.updateBookingStatusByStaff(id, newStatus);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/checkin")
    public ResponseEntity<BookingResponse> checkIn(@PathVariable Long id) {
        BookingResponse response = bookingService.checkInBooking(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<BookingResponse> checkOut(@PathVariable Long id) {
        BookingResponse response = bookingService.checkOutBooking(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','STAFF','SPECIALIST')")
    public ResponseEntity<List<BookingResponse>> getBookingsForCurrentUser() {
        List<BookingResponse> responses = bookingService.getBookingsForCurrentUser();
        return ResponseEntity.ok(responses);
    }


}
