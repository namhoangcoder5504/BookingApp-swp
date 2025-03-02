package BookingService.BookingService.mapper;

import BookingService.BookingService.dto.response.WishlistResponse;
import BookingService.BookingService.entity.Wishlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WishlistMapper {
    @Mapping(source = "wishlistId", target = "wishlistId")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "service.serviceId", target = "serviceId")
    @Mapping(source = "service.name", target = "serviceName")
    WishlistResponse toResponse(Wishlist wishlist);
}