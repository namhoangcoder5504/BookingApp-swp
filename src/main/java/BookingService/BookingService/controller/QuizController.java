package BookingService.BookingService.controller;

import BookingService.BookingService.dto.request.QuizQuestionRequest;
import BookingService.BookingService.dto.response.QuizQuestionResponse;
import BookingService.BookingService.entity.QuizResult;
import BookingService.BookingService.entity.ServiceEntity;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.service.QuizService;
import BookingService.BookingService.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/recommended-services")
    public List<ServiceEntity> getRecommendedServices() {
        return quizService.getRecommendedServicesFromLatestQuiz();
    }

    @GetMapping("/questions-with-answers")
    public List<Map<String, Object>> getQuizQuestionsWithAnswers() {
        return quizService.getQuizQuestionsWithAnswers();
    }

    @PostMapping("/submit")
    public Map<String, Object> submitQuiz(@RequestBody Map<Long, Long> selectedAnswers) {
        return quizService.processQuiz(selectedAnswers);
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

    // --- CRUD endpoints cho QuizQuestion ---

    @PostMapping("/questions")
    public ResponseEntity<QuizQuestionResponse> createQuestion(
            @Valid @RequestBody QuizQuestionRequest request) {
        QuizQuestionResponse response = quizService.createQuestion(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/questions")
    public ResponseEntity<List<QuizQuestionResponse>> getAllQuestions() {
        List<QuizQuestionResponse> questions = quizService.getAllQuestions();
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions/{id}")
    public ResponseEntity<QuizQuestionResponse> getQuestionById(@PathVariable Long id) {
        QuizQuestionResponse response = quizService.getQuestionById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<QuizQuestionResponse> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuizQuestionRequest request) {
        QuizQuestionResponse response = quizService.updateQuestion(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id) {
        quizService.deleteQuestion(id);
        return ResponseEntity.ok("Question with ID " + id + " has been deleted successfully.");
    }
}