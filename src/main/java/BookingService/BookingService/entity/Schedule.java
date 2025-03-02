package BookingService.BookingService.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Table(name = "schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    Long scheduleId;

    @ManyToOne
    @JoinColumn(name = "specialist_id")
    User specialist;

    @Column(name = "date")
    LocalDate date;

    @Column(name = "time_slot")
    String timeSlot;

    @Column(name = "availability")
    Boolean availability;
}