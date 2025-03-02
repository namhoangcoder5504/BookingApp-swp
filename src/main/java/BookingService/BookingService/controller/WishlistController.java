package BookingService.BookingService.controller;

import BookingService.BookingService.dto.request.WishlistRequest;
import BookingService.BookingService.dto.response.WishlistResponse;
import BookingService.BookingService.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // Tạo mới wishlist
    @PostMapping
    public ResponseEntity<WishlistResponse> createWishlist(@Valid @RequestBody WishlistRequest wishlistRequest) {
        WishlistResponse createdWishlist = wishlistService.createWishlist(wishlistRequest);
        return ResponseEntity.status(201).body(createdWishlist);
    }

    // Lấy toàn bộ wishlist
    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getAllWishlist() {
        List<WishlistResponse> wishlists = wishlistService.getAllWishlist();
        return ResponseEntity.ok(wishlists);
    }

    // Lấy wishlist theo id
    @GetMapping("/{id}")
    public ResponseEntity<WishlistResponse> getWishlistById(@PathVariable Long id) {
        WishlistResponse wishlist = wishlistService.getWishlistById(id);
        return ResponseEntity.ok(wishlist);
    }

    // Cập nhật wishlist theo id
    @PutMapping("/{id}")
    public ResponseEntity<WishlistResponse> updateWishlist(@PathVariable Long id,
                                                           @Valid @RequestBody WishlistRequest wishlistRequest) {
        WishlistResponse updatedWishlist = wishlistService.updateWishlist(id, wishlistRequest);
        return ResponseEntity.ok(updatedWishlist);
    }

    // Xóa wishlist theo id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWishlist(@PathVariable Long id) {
        wishlistService.deleteWishlist(id);
        return ResponseEntity.noContent().build();
    }
}
