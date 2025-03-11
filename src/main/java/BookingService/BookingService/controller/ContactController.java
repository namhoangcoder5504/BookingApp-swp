package BookingService.BookingService.controller;

import BookingService.BookingService.dto.request.ContactRequest;
import BookingService.BookingService.dto.response.ContactResponse;
import BookingService.BookingService.enums.ContactStatus;
import BookingService.BookingService.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    // Gửi yêu cầu liên hệ (mọi người đều có thể gửi)
    @PostMapping
    public ResponseEntity<String> submitContact(@RequestBody ContactRequest request) {
        contactService.submitContactRequest(request);
        return ResponseEntity.ok("Yêu cầu của bạn đã được gửi thành công!");
    }

    // Lấy danh sách tất cả yêu cầu liên hệ (chỉ ADMIN và STAFF)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<ContactResponse>> getAllContacts() {
        List<ContactResponse> contacts = contactService.getAllContacts();
        return ResponseEntity.ok(contacts);
    }

    // Cập nhật trạng thái yêu cầu liên hệ (chỉ ADMIN và STAFF)
    @PutMapping("/{contactId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ContactResponse> updateContactStatus(
            @PathVariable Long contactId,
            @RequestParam ContactStatus status) {
        ContactResponse updatedContact = contactService.updateContactStatus(contactId, status);
        return ResponseEntity.ok(updatedContact);
    }
}