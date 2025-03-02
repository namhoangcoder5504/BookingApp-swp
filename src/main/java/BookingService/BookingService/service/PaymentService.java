package BookingService.BookingService.service;

import BookingService.BookingService.entity.Booking;
import BookingService.BookingService.entity.Payment;
import BookingService.BookingService.enums.PaymentStatus;
import BookingService.BookingService.repository.BookingRepository;
import BookingService.BookingService.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    BookingRepository bookingRepository;

    // Lưu Payment mới ở trạng thái PENDING
    public Payment createPayment(Booking booking, String paymentMethod) {
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice()) // Lấy tổng tiền từ booking
                .paymentMethod(paymentMethod)
                .status(PaymentStatus.PENDING)
                .transactionId(null) // Sẽ set sau khi có TxnRef?
                .paymentTime(null)
                .build();

        // Cập nhật booking -> set PaymentStatus = PENDING
        booking.setPaymentStatus(PaymentStatus.PENDING);
        bookingRepository.save(booking);

        return paymentRepository.save(payment);
    }

    // Cập nhật trạng thái Payment sau khi VNPAY callback
    public Payment updatePaymentStatus(String transactionId, PaymentStatus status) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found by transactionId: " + transactionId));

        payment.setStatus(status);
        payment.setPaymentTime(LocalDateTime.now());
        paymentRepository.save(payment);

        // Cập nhật booking.paymentStatus
        Booking booking = payment.getBooking();
        booking.setPaymentStatus(status);
        bookingRepository.save(booking);

        return payment;
    }
}
