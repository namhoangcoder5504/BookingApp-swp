package BookingService.BookingService.mapper;

import BookingService.BookingService.dto.request.ScheduleRequest;
import BookingService.BookingService.dto.response.ScheduleResponse;
import BookingService.BookingService.entity.Schedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    ScheduleMapper INSTANCE = Mappers.getMapper(ScheduleMapper.class);

    @Mapping(source = "specialist.userId", target = "specialistId")
    ScheduleResponse toResponse(Schedule schedule);

    @Mapping(target = "specialist", ignore = true)
    Schedule toEntity(ScheduleRequest request);
}