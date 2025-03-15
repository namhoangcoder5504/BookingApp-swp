package BookingService.BookingService.repository;


import BookingService.BookingService.entity.Booking;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.enums.BookingStatus;
import BookingService.BookingService.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookingDateBeforeAndStatusIn(LocalDate date, List<BookingStatus> statuses);
    List<Booking> findByCustomer(User customer);
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b WHERE b.specialist.userId = :specialistId " +
            "AND b.bookingDate = :bookingDate " +
            "AND b.timeSlot = :timeSlot " +
            "AND b.status != 'CANCELLED'")
    boolean existsBySpecialistUserIdAndBookingDateAndTimeSlot(
            @Param("specialistId") Long specialistId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("timeSlot") String timeSlot);
    boolean existsByCustomerAndBookingDateAndTimeSlotAndBookingIdNot(User customer, LocalDate bookingDate, String timeSlot, Long bookingId);
    List<Booking> findByStatusIn(List<BookingStatus> statuses);
    List<Booking> findBySpecialist(User specialist);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.bookingDate = :date AND b.status = :status")
    BigDecimal sumTotalPriceByBookingDateAndStatus(LocalDate date, BookingStatus status);
    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime threshold);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.bookingDate BETWEEN :startDate AND :endDate AND b.status = :status")
    BigDecimal sumTotalPriceByBookingDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, BookingStatus status);
    boolean existsBySpecialistUserIdAndBookingDateAndTimeSlotAndStatusInAndBookingIdNot(
            Long specialistId, LocalDate bookingDate, String timeSlot, List<BookingStatus> statuses, Long excludeBookingId);
    boolean existsByCustomerAndBookingDateAndTimeSlot(User customer, LocalDate bookingDate, String timeSlot);
}