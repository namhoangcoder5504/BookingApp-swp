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

    public FeedbackResponse createFeedback(FeedbackRequest feedbackRequest) {

        Booking booking = bookingRepository.findById(feedbackRequest.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));


        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.BOOKING_NOT_COMPLETED);
        }


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String currentUserEmail = authentication.getName();


        User customer = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));


        if (!booking.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
        }


        boolean feedbackExists = feedbackRepository.existsByBookingAndCustomer(booking, customer);
        if (feedbackExists) {
            throw new AppException(ErrorCode.FEEDBACK_ALREADY_EXISTS);
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


    public List<FeedbackResponse> getFeedbacksByBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        return feedbackRepository.findByBooking(booking).stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }


    public List<FeedbackResponse> getFeedbacksBySpecialist() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        return feedbackRepository.findBySpecialist(loggedInUser).stream()
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