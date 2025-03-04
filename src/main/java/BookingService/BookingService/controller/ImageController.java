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

    // Chỉ ADMIN mới được tạo ảnh cho service
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/service/{serviceId}")
    public ResponseEntity<ImageResponse> createImageForService(
            @PathVariable Long serviceId,
            @Valid @RequestBody ImageRequest request) {
        ImageResponse response = imageService.createImageForService(request, serviceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Chỉ ADMIN mới được tạo ảnh cho blog
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/blog/{blogId}")
    public ResponseEntity<ImageResponse> createImageForBlog(
            @PathVariable Long blogId,
            @Valid @RequestBody ImageRequest request) {
        ImageResponse response = imageService.createImageForBlog(request, blogId);
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
    public ResponseEntity<ImageResponse> updateImage(
            @PathVariable Long id,
            @Valid @RequestBody ImageRequest request) {
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

    // Lấy tất cả ảnh theo blogId (mở cho mọi người)
    @GetMapping("/blog/{blogId}")
    public ResponseEntity<List<ImageResponse>> getImagesByBlog(@PathVariable Long blogId) {
        List<ImageResponse> responses = imageService.getImagesByBlog(blogId);
        return ResponseEntity.ok(responses);
    }
}