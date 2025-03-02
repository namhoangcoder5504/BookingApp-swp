package BookingService.BookingService.controller;

import BookingService.BookingService.entity.QuizResult;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.entity.QuizQuestion;
import BookingService.BookingService.service.QuizService;
import BookingService.BookingService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final UserService userService;

    @PostMapping("/submit")
    public Map<String, Object> submitQuiz(@RequestBody Map<Long, Long> selectedAnswers) {
        return quizService.processQuiz(selectedAnswers); // Trả về loại da và danh sách dịch vụ gợi ý
    }

    @GetMapping("/history")
    public List<QuizResult> getQuizHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByEmail(authentication.getName());
        return quizService.getUserQuizHistory(user);
    }

    @PostMapping("/populate")
    public String populateQuizData() {
        quizService.populateQuizData();
        return "Quiz questions and answers have been added!";
    }

    // Endpoint để lấy danh sách câu hỏi
    @GetMapping("/questions")
    public List<QuizQuestion> getQuizQuestions() {
        return quizService.getQuizQuestions(); // Gọi qua phương thức trong QuizService
    }
}