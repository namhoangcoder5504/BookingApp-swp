package BookingService.BookingService.repository;

import BookingService.BookingService.entity.QuizResult;
import BookingService.BookingService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByUser(User user);
}