    package BookingService.BookingService.repository;

    import BookingService.BookingService.entity.Payment;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;

    import java.util.Optional;

    public interface PaymentRepository extends JpaRepository<Payment, Long> {
        Optional<Payment> findByTransactionId(String transactionId);

        @Query("SELECT p FROM Payment p WHERE p.booking.bookingId = :bookingId")
        Optional<Payment> findByBookingId(Long bookingId);
    }