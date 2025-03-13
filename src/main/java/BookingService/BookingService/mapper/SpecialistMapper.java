package BookingService.BookingService.mapper;

import BookingService.BookingService.dto.response.SpecialistResponse;
import BookingService.BookingService.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SpecialistMapper {
    SpecialistMapper INSTANCE = Mappers.getMapper(SpecialistMapper.class);

    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "images", source = "images")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    SpecialistResponse toSpecialistResponse(User user);
}