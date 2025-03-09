package BookingService.BookingService.mapper;

import BookingService.BookingService.dto.response.NotificationResponse;
import BookingService.BookingService.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toNotificationResponse(Notification notification);
}