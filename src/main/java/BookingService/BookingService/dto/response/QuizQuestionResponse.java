package BookingService.BookingService.dto.response;

import BookingService.BookingService.enums.SkinType;
import lombok.Data;

import java.util.List;

@Data
public class QuizQuestionResponse {
    private Long id;
    private String questionText;
    private SkinType skinType;
    private List<QuizAnswerResponse> answers; // Thêm danh sách đáp án
}