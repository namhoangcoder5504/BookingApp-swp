package BookingService.BookingService.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common Errors (1001-1010)
    INVALID_KEY(1001, "Invalid message key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User already exists", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1003, "Email is invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least 4 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not found", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    NAME_INVALID(1007, "Name must not be blank", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1008, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_INPUT(1009, "Input data is invalid or missing", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    // Booking/Service Related Errors (1011-1020)
    BOOKING_NOT_EXISTED(1011, "Booking not found", HttpStatus.NOT_FOUND),
    SERVICE_NOT_EXISTED(1012, "Service not found", HttpStatus.NOT_FOUND),
    SCHEDULE_NOT_FOUND(1013, "Schedule not found", HttpStatus.NOT_FOUND),
    SKIN_THERAPIST_NOT_EXISTED(1014, "Skin therapist not found", HttpStatus.NOT_FOUND),
    BOOKING_TIME_CONFLICT(1015, "Schedule is already booked", HttpStatus.BAD_REQUEST),
    NO_AVAILABLE_SPECIALIST(1016, "No available specialist", HttpStatus.NOT_FOUND),
    BOOKING_NOT_COMPLETED(1017, "Booking not completed", HttpStatus.NOT_FOUND),
    BOOKING_NOT_CONFIRMED(1018, "Booking must be confirmed", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_CHECKED_IN(1019, "Booking must be checked in", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_CHECKED_IN_BEFORE_PAYMENT(1020, "Booking must be checked in before payment", HttpStatus.BAD_REQUEST),

    // Blog/Wishlist/Feedback/Image Related Errors (1021-1030)
    BLOG_NOT_EXISTED(1021, "Blog not found", HttpStatus.NOT_FOUND),
    FEEDBACK_NOT_FOUND(1022, "Feedback not found", HttpStatus.NOT_FOUND),
    FEEDBACK_ALREADY_EXISTS(1023, "Feedback already exists", HttpStatus.BAD_REQUEST),
    FEEDBACK_ALREADY_DONE(1024, "Feedback already submitted", HttpStatus.BAD_REQUEST),
    WISHLIST_DUPLICATE(1025, "Wishlist item is already added", HttpStatus.BAD_REQUEST),
    WISHLIST_NOT_FOUND(1026, "Wishlist item not found", HttpStatus.NOT_FOUND),
    WISHLIST_NOT_ALLOWED(1027, "Wishlist action is not allowed", HttpStatus.UNAUTHORIZED),
    IMAGE_NOT_FOUND(1028, "Image not found", HttpStatus.NOT_FOUND),
    CONTACT_NOT_FOUND(1029, "Contact request not found", HttpStatus.NOT_FOUND),
    EMAIL_SENDING_FAILED(1030, "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),

    // Schedule/Time Slot Related Errors (1031-1040)
    INVALID_TIME_SLOT_FORMAT(1031, "Time slot format is invalid", HttpStatus.BAD_REQUEST),
    SCHEDULE_NOT_AVAILABLE(1032, "Schedule is not available", HttpStatus.BAD_REQUEST),
    TIME_SLOT_UNAVAILABLE(1033, "Time slot is already booked", HttpStatus.BAD_REQUEST),
    TIME_SLOT_OUTSIDE_WORKING_HOURS(1034, "Time slot is outside working hours", HttpStatus.BAD_REQUEST),
    BOOKING_DATE_IN_PAST(1035, "Booking date must be in the future", HttpStatus.BAD_REQUEST),
    BOOKING_DATE_TOO_FAR_IN_FUTURE(1036, "Cannot book more than 7 days from today", HttpStatus.BAD_REQUEST),
    BOOKING_DURATION_EXCEEDS_TIME_SLOT(1037, "Total service duration exceeds time slot", HttpStatus.BAD_REQUEST),
    BOOKING_CANCEL_TIME_EXPIRED(1038, "Cannot cancel booking less than 24 hours before start time", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(1039, "Invalid date range", HttpStatus.BAD_REQUEST),
    BOOKING_SERVICE_LIMIT_EXCEEDED(1040, "Maximum number of services per booking exceeded", HttpStatus.BAD_REQUEST),

    // Quiz Related Errors (1041-1050)
    QUIZ_QUESTION_NOT_FOUND(1041, "Quiz question not found", HttpStatus.NOT_FOUND),
    QUIZ_ANSWER_NOT_FOUND(1042, "Quiz answer not found", HttpStatus.NOT_FOUND),
    QUIZ_RESULT_NOT_FOUND(1043, "Quiz result not found", HttpStatus.NOT_FOUND),
    DATA_ALREADY_EXISTS(1044, "Data already exists", HttpStatus.BAD_REQUEST),

    // Payment Related Errors (1051-1060)
    PAYMENT_NOT_FOUND(1051, "Payment not found for this booking", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_COMPLETED(1052, "Payment has already been completed", HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_MISMATCH(1053, "Payment amount does not match booking total price", HttpStatus.BAD_REQUEST),

    // Authentication/Authorization Related Errors (1061-1070)
    INVALID_PASSWORD(1061, "Invalid password", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(1062, "Password does not match", HttpStatus.BAD_REQUEST),
    INVALID_ROLE(1063, "Invalid role", HttpStatus.BAD_REQUEST),
    SPECIALIST_NOT_ACTIVE(1064, "Specialist is not active", HttpStatus.BAD_REQUEST),
    NOT_A_SPECIALIST(1065, "User is not a specialist", HttpStatus.BAD_REQUEST),
    INVALID_STATUS(1066, "Invalid status, status must be ACTIVE or INACTIVE", HttpStatus.BAD_REQUEST),
    BOOKING_CANNOT_BE_CANCELLED(1067, "Booking cannot be cancelled", HttpStatus.BAD_REQUEST),
    BOOKING_STATUS_INVALID(1068, "Booking status must be PENDING", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}