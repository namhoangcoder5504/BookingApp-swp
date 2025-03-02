package BookingService.BookingService.controller;

import BookingService.BookingService.dto.request.ImageRequest;
import BookingService.BookingService.dto.response.ImageResponse;
import BookingService.BookingService.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    // Chỉ ADMIN mới được tạo ảnh
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ImageResponse> createImage(@Valid @RequestBody ImageRequest request) {
        ImageResponse response = imageService.createImage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Lấy danh sách tất cả ảnh (mở cho mọi người)
    @GetMapping
    public ResponseEntity<List<ImageResponse>> getAllImages() {
        List<ImageResponse> responses = imageService.getAllImages();
        return ResponseEntity.ok(responses);
    }

    // Lấy ảnh theo ID (mở cho mọi người)
    @GetMapping("/{id}")
    public ResponseEntity<ImageResponse> getImageById(@PathVariable Long id) {
        ImageResponse response = imageService.getImageById(id);
        return ResponseEntity.ok(response);
    }

    // Chỉ ADMIN mới được cập nhật ảnh
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ImageResponse> updateImage(@PathVariable Long id, @Valid @RequestBody ImageRequest request) {
        ImageResponse response = imageService.updateImage(id, request);
        return ResponseEntity.ok(response);
    }

    // Chỉ ADMIN mới được xóa ảnh
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }

    // Lấy tất cả ảnh theo serviceId (mở cho mọi người)
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ImageResponse>> getImagesByService(@PathVariable Long serviceId) {
        List<ImageResponse> responses = imageService.getImagesByService(serviceId);
        return ResponseEntity.ok(responses);
    }
}
