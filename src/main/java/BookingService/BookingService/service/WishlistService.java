package BookingService.BookingService.service;

import BookingService.BookingService.dto.request.WishlistRequest;
import BookingService.BookingService.dto.response.WishlistResponse;
import BookingService.BookingService.entity.ServiceEntity;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.entity.Wishlist;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.mapper.WishlistMapper;
import BookingService.BookingService.repository.WishlistRepository;
import BookingService.BookingService.repository.UserRepository;
import BookingService.BookingService.repository.ServiceEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ServiceEntityRepository serviceRepository;
    private final WishlistMapper wishlistMapper;

    public WishlistResponse createWishlist(WishlistRequest wishlistRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String currentUserEmail = authentication.getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ServiceEntity service = serviceRepository.findById(wishlistRequest.getServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_EXISTED));

        if (!wishlistRepository.findByUserAndService(user, service).isEmpty()) {
            throw new AppException(ErrorCode.WISHLIST_DUPLICATE);
        }

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .service(service)
                .build();

        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        return wishlistMapper.toResponse(savedWishlist);
    }

    public List<WishlistResponse> getAllWishlist() {
        return wishlistRepository.findAll().stream()
                .map(wishlistMapper::toResponse)
                .collect(Collectors.toList());
    }

    public WishlistResponse getWishlistById(Long id) {
        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WISHLIST_NOT_FOUND));
        return wishlistMapper.toResponse(wishlist);
    }

    public WishlistResponse updateWishlist(Long id, WishlistRequest wishlistRequest) {
        Wishlist existingWishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WISHLIST_NOT_FOUND));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String currentUserEmail = authentication.getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!existingWishlist.getUser().getUserId().equals(user.getUserId())) {
            throw new AppException(ErrorCode.WISHLIST_NOT_ALLOWED);
        }

        ServiceEntity service = serviceRepository.findById(wishlistRequest.getServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_EXISTED));

        if (!wishlistRepository.findByUserAndService(user, service).stream()
                .filter(w -> !w.getWishlistId().equals(id))
                .collect(Collectors.toList()).isEmpty()) {
            throw new AppException(ErrorCode.WISHLIST_DUPLICATE);
        }

        existingWishlist.setService(service);
        Wishlist updatedWishlist = wishlistRepository.save(existingWishlist);
        return wishlistMapper.toResponse(updatedWishlist);
    }

    public void deleteWishlist(Long id) {
        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WISHLIST_NOT_FOUND));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String currentUserEmail = authentication.getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!wishlist.getUser().getUserId().equals(user.getUserId())) {
            throw new AppException(ErrorCode.WISHLIST_NOT_ALLOWED);
        }

        wishlistRepository.delete(wishlist);
    }
}