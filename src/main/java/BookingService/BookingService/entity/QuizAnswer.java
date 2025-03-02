package BookingService.BookingService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String answer;

    private int score; // Điểm số cho loại da khi chọn câu trả lời này

    @ManyToOne
    @JoinColumn(name = "question_id")
    private QuizQuestion question;
}
