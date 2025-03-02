package BookingService.BookingService.mapper;

import BookingService.BookingService.dto.request.ImageRequest;
import BookingService.BookingService.dto.response.ImageResponse;
import BookingService.BookingService.entity.Image;
import BookingService.BookingService.entity.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    @Mapping(target = "imageId", ignore = true) // ID is auto-generated
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())") // Set current time
    @Mapping(target = "service", source = "service") // Explicitly map service parameter
    @Mapping(target = "url", source = "request.url") // Map url from request
    Image toEntity(ImageRequest request, ServiceEntity service);

    @Mapping(target = "serviceId", source = "service.serviceId") // Map service ID from ServiceEntity
    ImageResponse toResponse(Image image);
}