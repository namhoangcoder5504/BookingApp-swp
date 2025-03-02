package BookingService.BookingService.repository;

import BookingService.BookingService.entity.Image;
import BookingService.BookingService.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByService(ServiceEntity service);
}
