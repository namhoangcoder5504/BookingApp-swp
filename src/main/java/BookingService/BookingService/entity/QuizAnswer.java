package BookingService.BookingService.entity;

import BookingService.BookingService.enums.SkinType;
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

    private int score;

    @Enumerated(EnumType.STRING)
    private SkinType skinType;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private QuizQuestion question;
}