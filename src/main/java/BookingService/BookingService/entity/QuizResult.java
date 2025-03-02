package BookingService.BookingService.entity;

import BookingService.BookingService.enums.SkinType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private SkinType detectedSkinType; // Loại da được phát hiện

    private String recommendedService; // Dịch vụ gợi ý

    private LocalDateTime createdAt;
}
