package BookingService.BookingService.repository;

import BookingService.BookingService.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    boolean existsBySpecialistUserIdAndDateAndTimeSlot(Long userId, LocalDate date, String timeSlot);
    boolean existsBySpecialistUserIdAndDateAndTimeSlotAndScheduleIdNot(Long userId, LocalDate date, String timeSlot, Long scheduleId);
    List<Schedule> findBySpecialistUserIdAndDate(Long specialistId, LocalDate date); // Thêm mới
}
