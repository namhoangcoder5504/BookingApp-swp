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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ScheduleMapper scheduleMapper;

    public User getSpecialistById(Long specialistId) {
        return userRepository.findById(specialistId)
                .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));
    }

    // Tạo nhiều lịch cùng lúc
    public List<Schedule> createSchedules(List<Schedule> schedules) {
        // Kiểm tra trùng lặp cho toàn bộ danh sách
        for (Schedule schedule : schedules) {
            Long specialistId = schedule.getSpecialist().getUserId();
            LocalDate date = schedule.getDate();
            String timeSlot = schedule.getTimeSlot();

            validateTimeSlot(timeSlot);
            if (scheduleRepository.existsBySpecialistUserIdAndDateAndTimeSlot(specialistId, date, timeSlot)) {
                throw new AppException(ErrorCode.BOOKING_TIME_CONFLICT);
            }
        }

        // Lưu tất cả lịch cùng lúc
        return scheduleRepository.saveAll(schedules);
    }

    public Schedule createSchedule(Schedule schedule) {
        Long specialistId = schedule.getSpecialist().getUserId();
        LocalDate date = schedule.getDate();
        String timeSlot = schedule.getTimeSlot();

        validateTimeSlot(timeSlot);
        boolean isConflict = scheduleRepository.existsBySpecialistUserIdAndDateAndTimeSlot(specialistId, date, timeSlot);
        if (isConflict) {
            throw new AppException(ErrorCode.BOOKING_TIME_CONFLICT);
        }

        return scheduleRepository.save(schedule);
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
            throw new AppException(ErrorCode.BOOKING_TIME_CONFLICT);
        }
        String[] times = timeSlot.split("-");
        String startTime = times[0];
        String endTime = times[1];
        if (startTime.compareTo(endTime) >= 0) {
            throw new AppException(ErrorCode.BOOKING_TIME_CONFLICT);
        }
    }
}