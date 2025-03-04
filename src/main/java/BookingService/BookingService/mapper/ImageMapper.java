package BookingService.BookingService.mapper;

import BookingService.BookingService.dto.request.ImageRequest;
import BookingService.BookingService.dto.response.ImageResponse;
import BookingService.BookingService.entity.Image;
import BookingService.BookingService.entity.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    @Mapping(target = "imageId", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "service", source = "service")
    @Mapping(target = "url", source = "request.url")
    @Mapping(target = "blog", ignore = true) // Không map blog ở đây vì dùng cho service
    Image toEntity(ImageRequest request, ServiceEntity service);

    @Mapping(target = "serviceId", source = "service.serviceId")
    @Mapping(target = "blogId", source = "blog.blogId") // Map blogId từ Blog
    ImageResponse toResponse(Image image);
}