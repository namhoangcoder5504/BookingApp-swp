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


    @Mapping(source = "scheduleId", target = "scheduleId")
    @Mapping(source = "specialist.userId", target = "specialistId")
    @Mapping(source = "specialist.name", target = "specialistName")
    @Mapping(source = "date", target = "date")
    @Mapping(source = "timeSlot", target = "timeSlot")
    @Mapping(source = "availability", target = "availability")
    ScheduleResponse toResponse(Schedule schedule);


    @Mapping(target = "specialist", ignore = true)
    @Mapping(source = "date", target = "date")
    @Mapping(source = "timeSlot", target = "timeSlot")
    @Mapping(source = "availability", target = "availability")
    Schedule toEntity(ScheduleRequest request);
}