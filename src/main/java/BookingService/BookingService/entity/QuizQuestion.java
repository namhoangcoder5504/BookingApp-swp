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
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    @Enumerated(EnumType.STRING)
    private SkinType skinType; // Loại da liên quan đến câu hỏi
}