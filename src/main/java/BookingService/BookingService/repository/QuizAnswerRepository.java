package BookingService.BookingService.repository;

import BookingService.BookingService.entity.QuizAnswer;
import BookingService.BookingService.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    List<QuizAnswer> findByQuestion(QuizQuestion question);
}