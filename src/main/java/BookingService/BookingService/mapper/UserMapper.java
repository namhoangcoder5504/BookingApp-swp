package BookingService.BookingService.mapper;

import BookingService.BookingService.dto.request.UserCreationRequest;
import BookingService.BookingService.dto.request.UserUpdateRequest;
import BookingService.BookingService.dto.response.UserResponse;
import BookingService.BookingService.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface UserMapper {
    User toUser(UserCreationRequest request);

    @Mapping(target = "images", source = "images")
    UserResponse toUserResponse(User user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "name", source = "name", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "phone", source = "phone", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "address", source = "address", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}