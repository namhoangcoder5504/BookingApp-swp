package BookingService.BookingService.controller;

import BookingService.BookingService.dto.request.FeedbackRequest;
import BookingService.BookingService.dto.response.FeedbackResponse;
import BookingService.BookingService.service.FeedBackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedBackService feedBackService;

    // Tạo feedback cho một booking
    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FeedbackResponse> createFeedback(@RequestBody FeedbackRequest feedbackRequest) {
        FeedbackResponse feedbackResponse = feedBackService.createFeedback(feedbackRequest);
        return ResponseEntity.ok(feedbackResponse);
    }

    // Lấy tất cả feedback cho một booking
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksByBooking(@PathVariable Long bookingId) {
        List<FeedbackResponse> feedbacks = feedBackService.getFeedbacksByBooking(bookingId);
        return ResponseEntity.ok(feedbacks);
    }

    // Lấy feedback của specialist (chỉ specialist được xem feedback của chính mình)
    @GetMapping("/specialist/{specialistId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksBySpecialist(@PathVariable Long specialistId) {
        List<FeedbackResponse> feedbacks = feedBackService.getFeedbacksBySpecialist(specialistId);
        return ResponseEntity.ok(feedbacks);
    }

    // Lấy tất cả feedback (cho admin và staff quản lý)
    @GetMapping()
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<List<FeedbackResponse>> getAllFeedback() {
        List<FeedbackResponse> feedbacks = feedBackService.getAllFeedback();
        return ResponseEntity.ok(feedbacks);
    }

    // Cập nhật feedback
    @PutMapping("/{feedbackId}")
    public ResponseEntity<FeedbackResponse> updateFeedback(
            @PathVariable Long feedbackId,
            @RequestBody FeedbackRequest feedbackRequest
    ) {
        FeedbackResponse updatedFeedback = feedBackService.updateFeedback(feedbackId,
                feedbackRequest.getRating(), feedbackRequest.getComment());
        return ResponseEntity.ok(updatedFeedback);
    }

    // Xóa feedback
    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long feedbackId) {
        feedBackService.deleteFeedback(feedbackId);
        return ResponseEntity.noContent().build();
    }
}
