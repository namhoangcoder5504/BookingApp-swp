package BookingService.BookingService.service;

import BookingService.BookingService.dto.request.ImageRequest;
import BookingService.BookingService.dto.response.ImageResponse;
import BookingService.BookingService.entity.Image;
import BookingService.BookingService.entity.ServiceEntity;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.mapper.ImageMapper;
import BookingService.BookingService.repository.ImageRepository;
import BookingService.BookingService.repository.ServiceEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ServiceEntityRepository serviceRepository;
    private final ImageMapper imageMapper;

    // Tạo mới Image, chỉ ADMIN mới được tạo
    public ImageResponse createImage(ImageRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() ||
                auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_EXISTED));
        Image image = imageMapper.toEntity(request, service);
        Image savedImage = imageRepository.save(image);
        return imageMapper.toResponse(savedImage);
    }

    public List<ImageResponse> getAllImages() {
        return imageRepository.findAll().stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ImageResponse getImageById(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        return imageMapper.toResponse(image);
    }

    public ImageResponse updateImage(Long id, ImageRequest request) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_EXISTED));
        image.setUrl(request.getUrl());
        image.setService(service);
        Image updatedImage = imageRepository.save(image);
        return imageMapper.toResponse(updatedImage);
    }

    public void deleteImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        imageRepository.delete(image);
    }

    public List<ImageResponse> getImagesByService(Long serviceId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_EXISTED));
        List<Image> images = imageRepository.findByService(service);
        return images.stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }
}
