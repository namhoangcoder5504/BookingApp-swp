package BookingService.BookingService.repository;

import BookingService.BookingService.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    boolean existsBySpecialistUserIdAndDateAndTimeSlotAndScheduleIdNot(Long userId, LocalDate date, String timeSlot, Long scheduleId);
    List<Schedule> findBySpecialistUserIdAndDate(Long specialistId, LocalDate date); //
    List<Schedule> findByAvailabilityFalseAndDateGreaterThanEqual(LocalDate date);
    List<Schedule> findBySpecialistUserIdAndAvailabilityFalseAndDateGreaterThanEqual(
            Long specialistId,
            LocalDate date
    );
}
