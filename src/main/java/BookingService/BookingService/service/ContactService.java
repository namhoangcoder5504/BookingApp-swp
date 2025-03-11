// BookingService.BookingService.service.ContactService.java
package BookingService.BookingService.service;

import BookingService.BookingService.dto.request.ContactRequest;
import BookingService.BookingService.dto.response.ContactResponse;
import BookingService.BookingService.entity.Contact;
import BookingService.BookingService.enums.ContactStatus;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final EmailService emailService;

    @Value("${spring.mail.username}") // Lấy email mặc định từ cấu hình
    private String supportEmail;

    // Gửi yêu cầu liên hệ (lưu vào DB và gửi email)
    @Transactional
    public void submitContactRequest(ContactRequest request) {
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        // Lưu yêu cầu vào database
        Contact contact = new Contact();
        contact.setFullName(request.getFullName());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setEmail(request.getEmail());
        contact.setMessage(request.getMessage());
        contact.setStatus(ContactStatus.PENDING);
        contactRepository.save(contact);

        // Gửi email thông báo cho admin/staff
        String subject = "Yêu cầu tư vấn từ " + request.getFullName();
        String htmlBody = buildContactEmail(request.getFullName(), request.getPhoneNumber(), request.getEmail(), request.getMessage());
        try {
            emailService.sendEmail(supportEmail, subject, htmlBody); // Gửi đến email hỗ trợ
            // Gửi email xác nhận cho khách hàng
            emailService.sendEmail(request.getEmail(), "Xác nhận yêu cầu tư vấn",
                    "Cảm ơn bạn " + request.getFullName() + ". Yêu cầu của bạn đã được gửi thành công. Chúng tôi sẽ liên hệ lại sớm!");
        } catch (Exception e) {
            throw new AppException(ErrorCode.EMAIL_SENDING_FAILED); // Chỉ truyền ErrorCode
        }
    }

    // Lấy danh sách tất cả yêu cầu liên hệ (chỉ dành cho ADMIN và STAFF)
    public List<ContactResponse> getAllContacts() {
        return contactRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Cập nhật trạng thái yêu cầu liên hệ (chỉ dành cho ADMIN và STAFF)
    @Transactional
    public ContactResponse updateContactStatus(Long contactId, ContactStatus status) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTACT_NOT_FOUND));
        contact.setStatus(status);
        contactRepository.save(contact);
        return mapToResponse(contact);
    }

    // Chuyển đổi từ Entity sang DTO
    private ContactResponse mapToResponse(Contact contact) {
        ContactResponse response = new ContactResponse();
        response.setId(contact.getId());
        response.setFullName(contact.getFullName());
        response.setPhoneNumber(contact.getPhoneNumber());
        response.setEmail(contact.getEmail());
        response.setMessage(contact.getMessage());
        response.setStatus(contact.getStatus());
        response.setCreatedAt(contact.getCreatedAt());
        response.setUpdatedAt(contact.getUpdatedAt());
        return response;
    }

    // Phương thức xây dựng email cho yêu cầu liên hệ
    private String buildContactEmail(String fullName, String phoneNumber, String email, String message) {
        return "<!DOCTYPE html>" +
                "<html><head><style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                ".header { background-color: #ff7e9d; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { padding: 20px; background-color: white; border-radius: 0 0 5px 5px; }" +
                ".footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h2>Yêu Cầu Tư Vấn</h2></div>" +
                "<div class='content'>" +
                "<p><strong>Họ và tên:</strong> " + fullName + "</p>" +
                "<p><strong>Số điện thoại:</strong> " + phoneNumber + "</p>" +
                "<p><strong>Email:</strong> " + email + "</p>" +
                "<p><strong>Ý kiến:</strong> " + message + "</p>" +
                "<p>Chúng tôi sẽ liên hệ lại với bạn trong thời gian sớm nhất. Cảm ơn bạn!</p>" +
                "</div>" +
                "<div class='footer'>© 2025 Beautya. All rights reserved.</div>" +
                "</div></body></html>";
    }
}