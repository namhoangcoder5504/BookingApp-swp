package BookingService.BookingService.repository;

import BookingService.BookingService.entity.BookingServices;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingServicesRepository extends JpaRepository<BookingServices, Long> {
}