package BookingService.BookingService.service;

import BookingService.BookingService.entity.Blog;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.repository.BlogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BlogService {

    @Autowired
    private BlogRepository blogRepository;

    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    public Optional<Blog> getBlogById(Long id) {
        return blogRepository.findById(id);
    }

    public Blog createBlog(Blog blog) {
        // Set blog cho từng image nếu có
        if (blog.getImages() != null) {
            blog.getImages().forEach(image -> image.setBlog(blog));
        }
        return blogRepository.save(blog);
    }

    public Blog updateBlog(Long id, Blog blogDetails) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_EXISTED));

        blog.setTitle(blogDetails.getTitle());
        blog.setContent(blogDetails.getContent());
        blog.setAuthor(blogDetails.getAuthor());

        // Xử lý images
        if (blogDetails.getImages() != null) {
            // Xóa images cũ nếu cần
            if (blog.getImages() != null) {
                blog.getImages().clear();
            }
            // Thêm images mới và set quan hệ
            blogDetails.getImages().forEach(image -> image.setBlog(blog));
            blog.setImages(blogDetails.getImages());
        }

        return blogRepository.save(blog);
    }

    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }
}