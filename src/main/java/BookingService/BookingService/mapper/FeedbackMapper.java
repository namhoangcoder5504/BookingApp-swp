package BookingService.BookingService.mapper;

import BookingService.BookingService.dto.response.FeedbackResponse;
import BookingService.BookingService.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    @Mapping(source = "feedbackId", target = "feedbackId")
    @Mapping(source = "booking.bookingId", target = "bookingId")
    @Mapping(source = "customer.userId", target = "customerId")
    @Mapping(source = "specialist.userId", target = "specialistId")
    @Mapping(source = "rating", target = "rating")
    @Mapping(source = "comment", target = "comment")
    @Mapping(source = "createdAt", target = "createdAt")
    FeedbackResponse toResponse(Feedback feedback);
}