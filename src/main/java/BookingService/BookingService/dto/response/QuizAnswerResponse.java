package BookingService.BookingService.dto.response;

import BookingService.BookingService.enums.SkinType;
import lombok.Data;

@Data
public class QuizAnswerResponse {
    private Long id;
    private String answerText;
    private int score;
    private SkinType skinType; // Thêm trường skinType
}