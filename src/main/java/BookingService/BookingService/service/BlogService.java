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

    /**
     * Lấy danh sách tất cả Blog
     */
    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    /**
     * Tìm Blog theo id
     */
    public Optional<Blog> getBlogById(Long id) {
        return blogRepository.findById(id);
    }

    /**
     * Tạo mới Blog
     */
    public Blog createBlog(Blog blog) {
        // Nếu sử dụng lifecycle callback thì createdAt, updatedAt sẽ tự động set
        return blogRepository.save(blog);
    }

    /**
     * Cập nhật Blog
     */
    public Blog updateBlog(Long id, Blog blogDetails) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_EXISTED));
        blog.setTitle(blogDetails.getTitle());
        blog.setContent(blogDetails.getContent());
        blog.setAuthor(blogDetails.getAuthor());
        // Nếu dùng @PreUpdate trong entity, updatedAt sẽ được tự động cập nhật
        return blogRepository.save(blog);
    }

    /**
     * Xoá Blog theo id
     */
    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }
}
