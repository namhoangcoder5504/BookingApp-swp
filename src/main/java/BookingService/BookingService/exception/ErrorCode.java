package BookingService.BookingService.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_EMAIL(1003, "Email is invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least 4 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    SERVICE_NOT_EXISTED(1012, "Service not existed", HttpStatus.NOT_FOUND),
    BOOKING_NOT_EXISTED(1011, "Booking not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    INVALID_KEY(1001, "Invalid message key", HttpStatus.BAD_REQUEST),
    SKIN_THERAPIST_NOT_EXISTED(1023, "Skin therapist not existed", HttpStatus.NOT_FOUND),
    SCHEDULE_NOT_FOUND(1022, "Schedule not existed", HttpStatus.NOT_FOUND),
    BOOKING_TIME_CONFLICT(1024, "Schedule is booked", HttpStatus.NOT_FOUND),
    NO_AVAILABLE_SPECIALIST(1025, "No available specialist", HttpStatus.NOT_FOUND),
    BOOKING_NOT_COMPLETED(1027, "Booking not complete", HttpStatus.NOT_FOUND),
    FEEDBACK_NOT_FOUND(1028, "FeedBack not existed", HttpStatus.NOT_FOUND),
    WISHLIST_DUPLICATE(1029, "Wishlist is duplicated", HttpStatus.NOT_FOUND),
    WISHLIST_NOT_FOUND(1030, "Wishlist is not found", HttpStatus.NOT_FOUND),
    IMAGE_NOT_FOUND(1032, "Image is not found", HttpStatus.NOT_FOUND),
    WISHLIST_NOT_ALLOWED(1031, "Wishlist is not allowed", HttpStatus.UNAUTHORIZED),

    BLOG_NOT_EXISTED(1026, "Blog not existed", HttpStatus.NOT_FOUND),
    NAME_INVALID(1007, "Name must not be blank", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1008, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_TIME_SLOT_FORMAT(1033, "Time slot format is invalid", HttpStatus.BAD_REQUEST),
    SCHEDULE_NOT_AVAILABLE(1034, "Schedule is not available", HttpStatus.BAD_REQUEST),
    TIME_SLOT_UNAVAILABLE(1035, "Time slot is already booked", HttpStatus.BAD_REQUEST),
    BOOKING_SERVICE_LIMIT_EXCEEDED(1040, "Maximum number of services per booking exceeded", HttpStatus.BAD_REQUEST),
    BOOKING_DATE_IN_PAST(1041, "Booking date must be in the future", HttpStatus.BAD_REQUEST),
    BOOKING_DURATION_EXCEEDS_TIME_SLOT(1042, "Total service duration exceeds time slot", HttpStatus.BAD_REQUEST),
    BOOKING_CANCEL_TIME_EXPIRED(1043, "Cannot cancel booking less than 24 hours before start time", HttpStatus.BAD_REQUEST),

    // ✅ Thêm lỗi cho Quiz
    QUIZ_QUESTION_NOT_FOUND(1040, "Quiz question not found", HttpStatus.NOT_FOUND),
    DATA_ALREADY_EXISTS(1042, "Data already exists", HttpStatus.BAD_REQUEST),
    QUIZ_ANSWER_NOT_FOUND(1041, "Quiz answer not found", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
