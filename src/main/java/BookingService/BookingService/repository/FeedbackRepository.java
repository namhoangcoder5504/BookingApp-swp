package BookingService.BookingService.repository;

import BookingService.BookingService.entity.Booking;
import BookingService.BookingService.entity.Feedback;
import BookingService.BookingService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByBooking(Booking booking);
    List<Feedback> findBySpecialist(User specialist);
}