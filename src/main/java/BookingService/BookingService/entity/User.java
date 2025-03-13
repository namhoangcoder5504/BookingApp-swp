package BookingService.BookingService.entity;

import BookingService.BookingService.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") // Thêm ánh xạ rõ ràng tới cột user_id
    Long userId;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = true) // Cho phép null vì guest không cần password
    String password;

    @Column(name = "name")
    String name;

    String phone;
    String address;

    @Enumerated(EnumType.STRING)
    Role role;

    @OneToOne(mappedBy = "user")
    private ForgotPassword forgotPassword;

    @Column(name = "status")
    private String status = "ACTIVE";
    @OneToMany(mappedBy = "user")
    private List<Image> images;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}