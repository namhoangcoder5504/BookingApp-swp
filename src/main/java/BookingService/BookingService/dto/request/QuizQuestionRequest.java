package BookingService.BookingService.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class QuizQuestionRequest {
    @NotBlank(message = "Question text must not be blank")
    private String questionText;

    @NotEmpty(message = "At least one answer is required")
    @Size(min = 4, max = 4, message = "Exactly 4 answers are required")
    private List<QuizAnswerRequest> answers;
}