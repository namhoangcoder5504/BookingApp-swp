package BookingService.BookingService.entity;


import BookingService.BookingService.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
    Long userId;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false)
    String password;

    @Column(name = "name")

    String name;
    String phone;
    String address;

    @Enumerated(EnumType.STRING)
    Role role;

    @OneToOne(mappedBy = "user")
    private ForgotPassword forgotPassword;


    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

