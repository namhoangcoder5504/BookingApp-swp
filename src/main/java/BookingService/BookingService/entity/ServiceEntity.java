package BookingService.BookingService.entity;

import BookingService.BookingService.enums.SkinType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    Long serviceId;

    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    BigDecimal price;
    Integer duration;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    // Một dịch vụ có thể có nhiều hình ảnh
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    List<Image> images;

    // Danh sách loại da phù hợp với dịch vụ
    @ElementCollection(targetClass = SkinType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "service_skin_types", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "skin_type")
    Set<SkinType> recommendedSkinTypes;
}