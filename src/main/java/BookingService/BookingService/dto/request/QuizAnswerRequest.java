package BookingService.BookingService.dto.request;

import BookingService.BookingService.enums.SkinType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class QuizAnswerRequest {
    @NotBlank(message = "Answer text must not be blank")
    private String answerText;

    @Min(value = 0, message = "Score must be non-negative")
    private int score;

    private SkinType skinType; // Thêm trường skinType
}