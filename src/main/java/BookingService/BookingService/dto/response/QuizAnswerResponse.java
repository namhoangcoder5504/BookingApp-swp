package BookingService.BookingService.dto.response;

import lombok.Data;

@Data
public class QuizAnswerResponse {
    private Long id;
    private String answerText;
    private int score;
}