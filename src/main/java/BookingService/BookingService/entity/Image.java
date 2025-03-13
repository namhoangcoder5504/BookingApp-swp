package BookingService.BookingService.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    Long imageId;

    @Column(nullable = false)
    String url;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "service_id", referencedColumnName = "service_id")
    @JsonBackReference
    ServiceEntity service;

    @ManyToOne
    @JoinColumn(name = "blog_id", referencedColumnName = "blog_id")
    @JsonBackReference
    Blog blog;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id") // Tham chiếu tới cột user_id trong bảng users
    @JsonBackReference
    User user;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}