package BookingService.BookingService.configuration;


import BookingService.BookingService.entity.User;
import BookingService.BookingService.enums.Role;
import BookingService.BookingService.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            String adminEmail = "admin@booking.com";
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User adminUser = User.builder()
                        .email(adminEmail)
                        .password(passwordEncoder.encode("admin"))
                        .name("Administrator")
                        .phone("000-000-0000")
                        .address("N/A")
                        .role(Role.ADMIN)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                userRepository.save(adminUser);
                log.warn("Admin user has been created with default password: admin");
            }
        };
    }
}
