package BookingService.BookingService.mapper;

import BookingService.BookingService.dto.request.BookingRequest;
import BookingService.BookingService.dto.response.BookingResponse;
import BookingService.BookingService.entity.Booking;
import BookingService.BookingService.entity.ServiceEntity;
import BookingService.BookingService.entity.User;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
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
    default void mapServiceNamesAndCustomerName(@MappingTarget BookingResponse response, Booking booking) {
        if (booking.getServices() != null && !booking.getServices().isEmpty()) {
            // Ánh xạ danh sách tên dịch vụ
            response.setServiceNames(booking.getServices().stream()
                    .map(ServiceEntity::getName)
                    .collect(Collectors.toList()));

            // Ánh xạ duration cho từng dịch vụ
            Map<String, Integer> serviceDurations = new HashMap<>();
            // Ánh xạ price cho từng dịch vụ
            Map<String, BigDecimal> servicePrices = new HashMap<>();

            booking.getServices().forEach(service -> {
                serviceDurations.put(service.getName(), service.getDuration());
                servicePrices.put(service.getName(), service.getPrice()); // Thêm giá tiền
            });

            response.setServiceDurations(serviceDurations);
            response.setServicePrices(servicePrices);

            // Tính tổng thời gian của tất cả dịch vụ
            int totalDuration = booking.getServices().stream()
                    .mapToInt(ServiceEntity::getDuration)
                    .sum();
            response.setTotalDuration(totalDuration);
        }

        // Ánh xạ tên khách hàng và specialist
        if (booking.getCustomer() != null) {
            response.setCustomerName(booking.getCustomer().getName());
        }
        if (booking.getSpecialist() != null) {
            response.setSpecialistName(booking.getSpecialist().getName());
        }
    }
}