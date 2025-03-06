package BookingService.BookingService.service;

import BookingService.BookingService.entity.Schedule;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.mapper.ScheduleMapper;
import BookingService.BookingService.repository.ScheduleRepository;
import BookingService.BookingService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ScheduleMapper scheduleMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public User getSpecialistById(Long specialistId) {
        return userRepository.findById(specialistId)
                .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));
    }

    public Schedule updateSchedule(Schedule existingSchedule, Schedule newData) {
        boolean isDateChanged = !existingSchedule.getDate().equals(newData.getDate());
        boolean isTimeSlotChanged = !existingSchedule.getTimeSlot().equals(newData.getTimeSlot());
        boolean isSpecialistChanged = !existingSchedule.getSpecialist().getUserId()
                .equals(newData.getSpecialist().getUserId());

        if (isDateChanged || isTimeSlotChanged || isSpecialistChanged) {
            validateTimeSlot(newData.getTimeSlot());
            boolean isConflict = scheduleRepository.existsBySpecialistUserIdAndDateAndTimeSlotAndScheduleIdNot(
                    newData.getSpecialist().getUserId(),
                    newData.getDate(),
                    newData.getTimeSlot(),
                    existingSchedule.getScheduleId()
            );
            if (isConflict) {
                throw new AppException(ErrorCode.BOOKING_TIME_CONFLICT);
            }
        }

        existingSchedule.setDate(newData.getDate());
        existingSchedule.setTimeSlot(newData.getTimeSlot());
        existingSchedule.setSpecialist(newData.getSpecialist());
        existingSchedule.setAvailability(newData.getAvailability());

        return scheduleRepository.save(existingSchedule);
    }

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new AppException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
        scheduleRepository.deleteById(id);
    }

    public List<Schedule> getSchedulesBySpecialistAndDate(Long specialistId, LocalDate date) {
        return scheduleRepository.findBySpecialistUserIdAndDate(specialistId, date);
    }

    private void validateTimeSlot(String timeSlot) {
        if (timeSlot == null || !timeSlot.matches("\\d{2}:\\d{2}-\\d{2}:\\d{2}")) {
            throw new AppException(ErrorCode.INVALID_TIME_SLOT_FORMAT); // Đổi sang mã lỗi phù hợp hơn
        }
        String[] times = timeSlot.split("-");
        LocalTime startTime = LocalTime.parse(times[0], TIME_FORMATTER);
        LocalTime endTime = LocalTime.parse(times[1], TIME_FORMATTER);
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new AppException(ErrorCode.INVALID_TIME_SLOT_FORMAT); // Đổi sang mã lỗi phù hợp hơn
        }
    }
}