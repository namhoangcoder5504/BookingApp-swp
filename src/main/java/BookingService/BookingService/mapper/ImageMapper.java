package BookingService.BookingService.mapper;

import BookingService.BookingService.dto.request.ImageRequest;
import BookingService.BookingService.dto.response.ImageResponse;
import BookingService.BookingService.entity.Image;
import BookingService.BookingService.entity.ServiceEntity;
import BookingService.BookingService.entity.User;
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
    @Mapping(target = "imageId", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "blog", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "url", source = "request.url")
    Image toEntity(ImageRequest request, User user);
    @Mapping(target = "serviceId", source = "service.serviceId")
    @Mapping(target = "blogId", source = "blog.blogId")
    @Mapping(target = "userId", source = "user.userId")
    ImageResponse toResponse(Image image);
}