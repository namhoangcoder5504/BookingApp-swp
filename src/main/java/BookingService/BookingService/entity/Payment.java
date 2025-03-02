package BookingService.BookingService.entity;

import BookingService.BookingService.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long paymentId;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    Booking booking;

    // Số tiền cần thanh toán (thông thường = booking.totalPrice)
    @Column(nullable = false)
    BigDecimal amount;

    // Cổng thanh toán sử dụng: VNPAY, MOMO, PAYPAL...
    @Column(name = "payment_method", nullable = false)
    String paymentMethod;

    // Mã giao dịch trả về từ VNPAY, đảm bảo duy nhất
    @Column(name = "transaction_id", unique = true, nullable = false)
    String transactionId;

    // PENDING, SUCCESS, FAILED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus status;

    // Thời điểm xác nhận thanh toán
    @Column(name = "payment_time")
    LocalDateTime paymentTime;
}