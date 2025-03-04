package BookingService.BookingService;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class BookingServiceApplication {
    private final Environment env;

    public BookingServiceApplication(Environment env) {
        this.env = env;
    }
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }

    @PostConstruct
    public void init() {
        if (env.acceptsProfiles("prod")) {
            System.setProperty("springdoc.api-docs.enabled", "false");
            System.setProperty("springdoc.swagger-ui.enabled", "false");
        }
    }
}
