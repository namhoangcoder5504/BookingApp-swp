package BookingService.BookingService.mapper;

import BookingService.BookingService.dto.request.ServiceEntityRequest;
import BookingService.BookingService.dto.response.ServiceEntityResponse;
import BookingService.BookingService.entity.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ServiceEntityMapper {

    @Mapping(target = "serviceId", ignore = true) // ID is auto-generated
    @Mapping(target = "createdAt", ignore = true) // Set in service layer
    @Mapping(target = "updatedAt", ignore = true) // Set in service layer
    @Mapping(target = "images", ignore = true) // Managed separately
    ServiceEntity toEntity(ServiceEntityRequest request);

    ServiceEntityResponse toResponse(ServiceEntity entity);
}