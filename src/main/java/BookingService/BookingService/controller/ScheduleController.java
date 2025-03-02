package BookingService.BookingService.controller;

import BookingService.BookingService.dto.request.ScheduleRequest;
import BookingService.BookingService.dto.response.ScheduleResponse;
import BookingService.BookingService.entity.Schedule;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.mapper.ScheduleMapper;
import BookingService.BookingService.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleMapper scheduleMapper;

    @PostMapping("/bulk")
    public ResponseEntity<List<ScheduleResponse>> createSchedules(@Valid @RequestBody List<ScheduleRequest> requests) {
        List<Schedule> schedules = requests.stream()
                .map(request -> {
                    User specialist = scheduleService.getSpecialistById(request.getSpecialistId());
                    Schedule schedule = scheduleMapper.toEntity(request);
                    schedule.setSpecialist(specialist);
                    return schedule;
                })
                .collect(Collectors.toList());

        List<Schedule> savedSchedules = scheduleService.createSchedules(schedules);
        List<ScheduleResponse> responses = savedSchedules.stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getAllSchedules() {
        List<ScheduleResponse> responseList = scheduleService.getAllSchedules()
                .stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getScheduleById(@PathVariable Long id) {
        return scheduleService.getScheduleById(id)
                .map(schedule -> ResponseEntity.ok(scheduleMapper.toResponse(schedule)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        User specialist = scheduleService.getSpecialistById(request.getSpecialistId());
        Schedule scheduleEntity = scheduleMapper.toEntity(request);
        scheduleEntity.setSpecialist(specialist);
        Schedule savedSchedule = scheduleService.createSchedule(scheduleEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleMapper.toResponse(savedSchedule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleRequest request) {
        return scheduleService.getScheduleById(id)
                .map(existingSchedule -> {
                    User specialist = scheduleService.getSpecialistById(request.getSpecialistId());
                    Schedule newData = scheduleMapper.toEntity(request);
                    newData.setSpecialist(specialist);
                    Schedule updatedSchedule = scheduleService.updateSchedule(existingSchedule, newData);
                    return ResponseEntity.ok(scheduleMapper.toResponse(updatedSchedule));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        try {
            scheduleService.deleteSchedule(id);
            return ResponseEntity.noContent().build();
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.SCHEDULE_NOT_FOUND) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/specialist/{specialistId}/date/{date}")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesBySpecialistAndDate(
            @PathVariable Long specialistId,
            @PathVariable("date") LocalDate date) {
        List<ScheduleResponse> responseList = scheduleService.getSchedulesBySpecialistAndDate(specialistId, date)
                .stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi: " + ex.getMessage());
    }
}