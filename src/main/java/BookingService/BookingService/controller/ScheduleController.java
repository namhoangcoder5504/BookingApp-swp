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
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
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
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
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

    // Xử lý lỗi cụ thể cho AppException
    @ExceptionHandler(AppException.class)
    public ResponseEntity<String> handleAppException(AppException ex) {
        if (ex.getErrorCode() == ErrorCode.SCHEDULE_NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lịch không tồn tại: " + ex.getMessage());
        } else if (ex.getErrorCode() == ErrorCode.BOOKING_TIME_CONFLICT) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Lịch đã trùng với lịch khác: " + ex.getMessage());
        } else if (ex.getErrorCode() == ErrorCode.INVALID_TIME_SLOT_FORMAT) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Định dạng khung giờ không hợp lệ: " + ex.getMessage());
        } else if (ex.getErrorCode() == ErrorCode.SKIN_THERAPIST_NOT_EXISTED) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Chuyên viên không tồn tại: " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi hệ thống: " + ex.getMessage());
    }

    // Xử lý lỗi chung khác
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Đã xảy ra lỗi không xác định: " + ex.getMessage());
    }
}