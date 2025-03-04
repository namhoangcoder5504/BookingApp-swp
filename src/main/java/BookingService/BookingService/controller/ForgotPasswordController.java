package BookingService.BookingService.controller;

import BookingService.BookingService.dto.request.ChangePassword;
import BookingService.BookingService.dto.request.MailBody;
import BookingService.BookingService.entity.ForgotPassword;
import BookingService.BookingService.entity.User;
import BookingService.BookingService.repository.UserRepository;
import BookingService.BookingService.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import BookingService.BookingService.repository.ForgotPasswordRepository;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/forgotPassword")
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordController(UserRepository userRepository, EmailService emailService, ForgotPasswordRepository forgotPasswordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Send mail for email verification
    @PostMapping("/verifyMail/{email}")
    public ResponseEntity<String> verifyEMail(@PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email!")); // Check if email exists

        int otp = otpGenerator();
        MailBody mailBody = MailBody.builder()
                .to(email)
                .text("This is the OTP for your Forgot Password request: " + otp)
                .subject("OTP for Forgot Password request")
                .build();

        // Kiểm tra xem đã có bản ghi ForgotPassword nào cho user này chưa
        Optional<ForgotPassword> existingFp = forgotPasswordRepository.findByUser(user);
        ForgotPassword fp;

        if (existingFp.isPresent()) {
            // Nếu đã tồn tại, cập nhật OTP và expirationTime
            fp = existingFp.get();
            fp.setOtp(otp);
            fp.setExpirationTime(new Date(System.currentTimeMillis() + 70 * 1000)); // Cập nhật thời gian hết hạn
        } else {
            // Nếu chưa tồn tại, tạo mới bản ghi ForgotPassword
            fp = ForgotPassword.builder()
                    .otp(otp)
                    .expirationTime(new Date(System.currentTimeMillis() + 70 * 1000))
                    .user(user)
                    .build();
        }

        emailService.sendSimpleMessage(mailBody);
        forgotPasswordRepository.save(fp); // Lưu hoặc cập nhật bản ghi

        return ResponseEntity.ok("Email sent for verification!");
    }

    @PostMapping("/verifyOtp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp, @PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email!"));

        Optional<ForgotPassword> fpOpt = forgotPasswordRepository.findByOtpAndUser(otp, user);
        if (!fpOpt.isPresent()) {
            return new ResponseEntity<>("Invalid OTP for email: " + email, HttpStatus.BAD_REQUEST);
        }

        ForgotPassword fp = fpOpt.get();
        if (fp.getExpirationTime().before(Date.from(Instant.now()))) {
            forgotPasswordRepository.deleteById(fp.getFpid());
            return new ResponseEntity<>("OTP has expired!", HttpStatus.EXPECTATION_FAILED);
        }
        return ResponseEntity.ok("OTP verified!");
    }

    @PostMapping("/changePassword/{email}")
    public ResponseEntity<String> changePasswordHandler(@RequestBody ChangePassword changePassword,
                                                        @PathVariable String email) {
        if (!Objects.equals(changePassword.password(), changePassword.repeatPassword())) {
            return new ResponseEntity<>("Please enter the password again!", HttpStatus.EXPECTATION_FAILED);
        }
        String encodedPassword = passwordEncoder.encode(changePassword.password());
        userRepository.updatePassword(email, encodedPassword);

        return ResponseEntity.ok("Password changed successfully!");
    }

    private Integer otpGenerator() {
        Random random = new Random();
        return random.nextInt(100_000, 999_999);
    }
}