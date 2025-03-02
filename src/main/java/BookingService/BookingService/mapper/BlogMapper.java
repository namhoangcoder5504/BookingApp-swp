package BookingService.BookingService.mapper;

import BookingService.BookingService.dto.response.BlogResponse;
import BookingService.BookingService.entity.Blog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface BlogMapper {
    @Mapping(source = "blogId", target = "blogId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "author", target = "author") // UserMapper sẽ xử lý author
    BlogResponse toResponse(Blog blog);
}