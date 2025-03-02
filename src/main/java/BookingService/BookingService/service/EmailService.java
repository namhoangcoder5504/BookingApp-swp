package BookingService.BookingService.service;

import BookingService.BookingService.dto.request.MailBody;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void sendSimpleMessage(MailBody mailBody) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailBody.to());
        // Sử dụng địa chỉ email được cấu hình làm người gửi
        message.setFrom(fromEmail);
        message.setSubject(mailBody.subject());
        message.setText(mailBody.text());
        javaMailSender.send(message);
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            // Tạo nội dung HTML cho email
            String htmlContent = "<!DOCTYPE html>" +
                    "<html lang=\"vi\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "<title>Beautya - Chăm Sóc Sắc Đẹp</title>" +
                    "<style>" +
                    "/* Reset CSS */" +
                    "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                    "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #333; background-color: #f8f6f5; line-height: 1.6; }" +
                    ".container { max-width: 600px; margin: 0 auto; background-color: white; box-shadow: 0 5px 20px rgba(0,0,0,0.05); }" +
                    ".header { background: linear-gradient(135deg, #e89b9b 0%, #d47a8c 100%); padding: 30px 20px; text-align: center; color: white; }" +
                    ".header h1 { font-size: 32px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; }" +
                    ".header p { font-size: 16px; opacity: 0.9; margin-top: 5px; }" +
                    ".content { padding: 30px; }" +
                    ".greeting { font-size: 22px; font-weight: 600; color: #d47a8c; margin-bottom: 15px; }" +
                    ".text-content { font-size: 16px; color: #555; }" +
                    ".text-content p { margin-bottom: 20px; }" +
                    ".highlight { background: linear-gradient(90deg, #f8e1e1 0%, #fcebeb 100%); padding: 15px; border-radius: 8px; margin: 20px 0; }" +
                    ".cta-button { display: inline-block; background: linear-gradient(135deg, #e89b9b 0%, #d47a8c 100%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 50px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; transition: all 0.3s; }" +
                    ".cta-button:hover { background: linear-gradient(135deg, #d47a8c 0%, #e89b9b 100%); }" +
                    ".signature { margin-top: 30px; padding-top: 20px; border-top: 1px dashed #eee; font-size: 14px; color: #777; }" +
                    ".social-icons { margin: 20px 0; text-align: center; }" +
                    ".social-icons a { color: #d47a8c; text-decoration: none; margin: 0 12px; font-size: 14px; font-weight: 500; }" +
                    ".footer { text-align: center; font-size: 12px; color: #999; padding: 20px; background-color: #fafafa; border-top: 1px solid #eee; }" +
                    "/* Responsive styles */" +
                    "@media only screen and (min-width: 481px) {" +
                    "    .container { margin: 30px auto; border-radius: 12px; overflow: hidden; }" +
                    "    .header h1 { font-size: 36px; }" +
                    "    .content { padding: 40px; }" +
                    "}" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class=\"container\">" +
                    "<div class=\"header\">" +
                    "<h1>Beautya</h1>" +
                    "<p>Chăm sóc sắc đẹp - Nâng niu vẻ đẹp của bạn</p>" +
                    "</div>" +
                    "<div class=\"content\">" +
                    "<div class=\"text-content\">" +
                    "<p>" + body + "</p>" + // Nội dung động từ tham số body
                    "<div class=\"highlight\">" +
                    "<p>Chúng tôi tin rằng mỗi người đều sở hữu vẻ đẹp riêng biệt, và Beautya ở đây để giúp bạn tỏa sáng theo cách đặc biệt nhất.</p>" +
                    "</div>" +
                    "<p>Hãy để chúng tôi đồng hành cùng bạn trong hành trình chăm sóc sắc đẹp với những giải pháp cá nhân hóa tốt nhất.</p>" +
                    "<a href=\"#\" class=\"cta-button\">Khám Phá Ngay</a>" +
                    "<div class=\"signature\">" +
                    "<p>Trân trọng,<br>Đội ngũ Beautya</p>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "<div class=\"social-icons\">" +
                    "<a href=\"#\">Facebook</a> • " +
                    "<a href=\"#\">Instagram</a> • " +
                    "<a href=\"#\">Twitter</a>" +
                    "</div>" +
                    "<div class=\"footer\">" +
                    "© 2025 Beatya Beauty. All rights reserved.<br>" +
                    "Địa chỉ: 456 Đường Sắc Đẹp, Quận 1, TP.HCM" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true); // true = gửi dưới dạng HTML

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage());
        }
    }
}
