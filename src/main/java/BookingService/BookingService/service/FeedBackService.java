package BookingService.BookingService.service;

import BookingService.BookingService.dto.request.FeedbackRequest;
import BookingService.BookingService.dto.response.FeedbackResponse;
import BookingService.BookingService.entity.Feedback;
import BookingService.BookingService.entity.Booking;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.enums.BookingStatus;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.mapper.FeedbackMapper;
import BookingService.BookingService.repository.FeedbackRepository;
import BookingService.BookingService.repository.BookingRepository;
import BookingService.BookingService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedBackService {

    private final FeedbackRepository feedbackRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final FeedbackMapper feedbackMapper;

    // Tạo feedback cho một booking (chỉ được phép nếu booking đã COMPLETED và customer trùng khớp)
    public FeedbackResponse createFeedback(FeedbackRequest feedbackRequest) {
        // Lấy booking theo bookingId
        Booking booking = bookingRepository.findById(feedbackRequest.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        // Kiểm tra trạng thái booking
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.BOOKING_NOT_COMPLETED);
        }

        // Lấy thông tin authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String currentUserEmail = authentication.getName();

        // Lấy thông tin customer từ repository
        User customer = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // Kiểm tra rằng customer của booking phải trùng với customer hiện tại
        if (!booking.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
        }

        // Lấy specialist từ booking
        User specialist = booking.getSpecialist();

        // Tạo Feedback
        Feedback feedback = Feedback.builder()
                .booking(booking)
                .customer(customer)
                .specialist(specialist)
                .rating(feedbackRequest.getRating())
                .comment(feedbackRequest.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Feedback savedFeedback = feedbackRepository.save(feedback);
        return feedbackMapper.toResponse(savedFeedback);
    }

    // Lấy feedback theo booking
    public List<FeedbackResponse> getFeedbacksByBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        return feedbackRepository.findByBooking(booking).stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Lấy feedback của specialist theo specialistId (specialist chỉ được xem feedback của chính mình)
    public List<FeedbackResponse> getFeedbacksBySpecialist(Long specialistId) {
        // Lấy thông tin user đang đăng nhập từ SecurityContext
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // Kiểm tra rằng specialistId truyền vào phải trùng với id của user đang đăng nhập
        if (!loggedInUser.getUserId().equals(specialistId)) {
            throw new AppException(ErrorCode.FEEDBACK_NOT_FOUND);
        }

        // Nếu hợp lệ, lấy specialist từ repository
        User specialist = userRepository.findById(specialistId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Lấy danh sách feedback của specialist đó
        return feedbackRepository.findBySpecialist(specialist).stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }


    // Lấy tất cả feedback (cho admin và staff quản lý)
    public List<FeedbackResponse> getAllFeedback() {
        return feedbackRepository.findAll().stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Cập nhật feedback
    public FeedbackResponse updateFeedback(Long feedbackId, int rating, String comment) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new AppException(ErrorCode.FEEDBACK_NOT_FOUND));

        feedback.setRating(rating);
        feedback.setComment(comment);
        // Cập nhật thời gian nếu cần (có thể dùng một trường updatedAt riêng nếu có)
        feedback.setCreatedAt(LocalDateTime.now());

        Feedback updatedFeedback = feedbackRepository.save(feedback);
        return feedbackMapper.toResponse(updatedFeedback);
    }

    // Xóa feedback
    public void deleteFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new AppException(ErrorCode.FEEDBACK_NOT_FOUND));

        feedbackRepository.delete(feedback);
    }
}
