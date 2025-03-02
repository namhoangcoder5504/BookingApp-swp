package BookingService.BookingService.repository;

import BookingService.BookingService.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ServiceEntityRepository extends JpaRepository<ServiceEntity, Long> {
    Optional<ServiceEntity> findByName(String name);

    @Query("SELECT s FROM ServiceEntity s WHERE LOWER(s.name) LIKE LOWER(concat('%', :name, '%'))")
    List<ServiceEntity> findByNameContainingIgnoreCase(String name);
}