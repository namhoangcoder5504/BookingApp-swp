package BookingService.BookingService.mapper;


import BookingService.BookingService.dto.request.UserCreationRequest;
import BookingService.BookingService.dto.request.UserUpdateRequest;
import BookingService.BookingService.dto.response.UserResponse;
import BookingService.BookingService.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);

}
