package BookingService.BookingService.dto.response;

import BookingService.BookingService.enums.SkinType;
import lombok.Data;

import java.util.List;

@Data
public class QuizQuestionResponse {
    private Long id;
    private String questionText;
//    private SkinType skinType; // Có thể để null nếu không cần
    private List<QuizAnswerResponse> answers;
}