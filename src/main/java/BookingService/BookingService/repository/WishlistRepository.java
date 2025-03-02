package BookingService.BookingService.repository;

import BookingService.BookingService.entity.ServiceEntity;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserAndService(User user, ServiceEntity service);
}