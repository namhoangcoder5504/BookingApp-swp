package BookingService.BookingService.service;

import BookingService.BookingService.dto.request.ImageRequest;
import BookingService.BookingService.dto.response.ImageResponse;
import BookingService.BookingService.entity.Blog;
import BookingService.BookingService.entity.Image;
import BookingService.BookingService.entity.ServiceEntity;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.mapper.ImageMapper;
import BookingService.BookingService.repository.BlogRepository;
import BookingService.BookingService.repository.ImageRepository;
import BookingService.BookingService.repository.ServiceEntityRepository;
import BookingService.BookingService.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ServiceEntityRepository serviceRepository;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private UserRepository userRepository; // Thêm repository cho User

    @Autowired
    private ImageMapper imageMapper;

    // Tạo ảnh cho service
    public ImageResponse createImageForService(ImageRequest request, Long serviceId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_EXISTED));
        Image image = imageMapper.toEntity(request, service);
        image.setBlog(null);
        image.setUser(null); // Đảm bảo không liên kết với user hoặc blog
        return imageMapper.toResponse(imageRepository.save(image));
    }

    // Tạo ảnh cho blog
    public ImageResponse createImageForBlog(ImageRequest request, Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_EXISTED));
        Image image = imageMapper.toEntity(request, (ServiceEntity) null);
        image.setBlog(blog);
        image.setUser(null); // Đảm bảo không liên kết với user
        return imageMapper.toResponse(imageRepository.save(image));
    }

    // Tạo ảnh cho user
    public ImageResponse createImageForUser(ImageRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Image image = imageMapper.toEntity(request, user);
        image.setService(null); // Đảm bảo không liên kết với service
        image.setBlog(null); // Đảm bảo không liên kết với blog
        return imageMapper.toResponse(imageRepository.save(image));
    }

    // Lấy tất cả ảnh
    public List<ImageResponse> getAllImages() {
        return imageRepository.findAll()
                .stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Lấy ảnh theo ID
    public ImageResponse getImageById(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        return imageMapper.toResponse(image);
    }

    // Cập nhật ảnh
    public ImageResponse updateImage(Long id, ImageRequest request) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        image.setUrl(request.getUrl());
        return imageMapper.toResponse(imageRepository.save(image));
    }

    // Xóa ảnh
    public void deleteImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        imageRepository.delete(image);
    }

    // Lấy ảnh theo serviceId
    public List<ImageResponse> getImagesByService(Long serviceId) {
        return imageRepository.findByServiceServiceId(serviceId)
                .stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Lấy ảnh theo blogId
    public List<ImageResponse> getImagesByBlog(Long blogId) {
        return imageRepository.findByBlogBlogId(blogId)
                .stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Lấy ảnh theo userId
    public List<ImageResponse> getImagesByUser(Long userId) {
        return imageRepository.findByUserUserId(userId)
                .stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }
}