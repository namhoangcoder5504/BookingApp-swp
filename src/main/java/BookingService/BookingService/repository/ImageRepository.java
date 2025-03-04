package BookingService.BookingService.repository;

import BookingService.BookingService.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByServiceServiceId(Long serviceId);
    List<Image> findByBlogBlogId(Long blogId);
}