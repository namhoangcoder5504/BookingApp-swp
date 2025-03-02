package BookingService.BookingService.entity;

import BookingService.BookingService.enums.BookingStatus;
import BookingService.BookingService.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    Long bookingId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    User customer;

    @ManyToOne
    @JoinColumn(name = "specialist_id")
    User specialist;

    @Column(name = "booking_date")
    LocalDate bookingDate;

    @Column(name = "time_slot")
    String timeSlot;

    @Enumerated(EnumType.STRING)
    BookingStatus status; // Ví dụ: NEW, CHECK_IN, IN_PROGRESS, COMPLETED

    @Column(name = "check_in_time")
    LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    LocalDateTime checkOutTime;

    @Column(name = "total_price")
    BigDecimal totalPrice;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;
    // PENDING (đang chờ thanh toán), SUCCESS (thanh toán thành công), FAILED (thất bại)...

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    Payment payment;
}
