package BookingService.BookingService.controller;

import BookingService.BookingService.dto.request.BlogRequest;
import BookingService.BookingService.dto.response.BlogResponse;
import BookingService.BookingService.entity.Blog;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.mapper.BlogMapper;
import BookingService.BookingService.service.BlogService;
import BookingService.BookingService.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private UserService userService;

    @Autowired
    private BlogMapper blogMapper;

    @GetMapping
    public ResponseEntity<List<BlogResponse>> getAllBlogs() {
        List<BlogResponse> responses = blogService.getAllBlogs()
                .stream()
                .map(blogMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> getBlogById(@PathVariable Long id) {
        return blogService.getBlogById(id)
                .map(blogMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<BlogResponse> createBlog(@Valid @RequestBody BlogRequest blogRequest) {
        User author = userService.getUserById(blogRequest.getAuthorId());
        if (author == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        Blog blog = Blog.builder()
                .title(blogRequest.getTitle())
                .content(blogRequest.getContent())
                .author(author)
                .build();
        Blog createdBlog = blogService.createBlog(blog);
        BlogResponse response = blogMapper.toResponse(createdBlog);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<BlogResponse> updateBlog(@PathVariable Long id, @Valid @RequestBody BlogRequest blogRequest) {
        User author = userService.getUserById(blogRequest.getAuthorId());
        if (author == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        Blog blogDetails = Blog.builder()
                .title(blogRequest.getTitle())
                .content(blogRequest.getContent())
                .author(author)
                .build();
        Blog updatedBlog = blogService.updateBlog(id, blogDetails);
        BlogResponse response = blogMapper.toResponse(updatedBlog);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }
}