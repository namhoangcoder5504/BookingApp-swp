package BookingService.BookingService.mapper;


import BookingService.BookingService.dto.request.BookingRequest;
import BookingService.BookingService.dto.response.BookingResponse;
import BookingService.BookingService.entity.Booking;
import BookingService.BookingService.entity.ServiceEntity;
import BookingService.BookingService.entity.User;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "customer.userId", target = "customerId")
    @Mapping(source = "specialist.userId", target = "specialistId")
    BookingResponse toResponse(Booking booking);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "specialist", ignore = true)
    Booking toEntity(BookingRequest request);

    @AfterMapping
    default void setUserEntities(@MappingTarget Booking booking, User customer, User specialist) {
        booking.setCustomer(customer);
        booking.setSpecialist(specialist);
    }
    @AfterMapping
    default void mapServiceNames(@MappingTarget BookingResponse response, Booking booking) {
        if (booking.getServices() != null && !booking.getServices().isEmpty()) {
            response.setServiceNames(booking.getServices().stream()
                    .map(ServiceEntity::getName)
                    .collect(Collectors.toList()));
        }
    }
}
