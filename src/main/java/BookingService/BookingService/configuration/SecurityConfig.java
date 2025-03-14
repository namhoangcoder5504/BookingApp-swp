package BookingService.BookingService.configuration;

import BookingService.BookingService.enums.Role;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/users",
            "/auth/token",
            "/auth/introspect",
            "/auth/logout",
            "/auth/refresh",
            "/forgotPassword/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/auth/google",
            "/oauth2/authorization/google",
            "/login/oauth2/code/google",
            "/api/v1/vnpay/payment-info",
            "/test",
            "/api/services",
            "/api/blogs",
            "/api/quiz/questions-with-answers",
            "/api/users/specialists",
            "/api/users/specialists/active",
            "/api/blogs/{id}",
            "/api/bookings/guest" ,
            "/api/schedules/busy",
            "/api/schedules/{specialistId}/busy"
    };

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // Cho phép tất cả request đến endpoints công khai mà không cần token
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                // Chỉ ADMIN mới được GET danh sách user
                .requestMatchers(HttpMethod.GET, "/users").hasRole(Role.ADMIN.name())
                // Chỉ ADMIN và STAFF được truy cập GET /api/contact
                .requestMatchers(HttpMethod.GET, "/api/contact").hasAnyRole(Role.ADMIN.name(), Role.STAFF.name())
                // Chỉ ADMIN và STAFF được truy cập PUT /api/contact/{id}/status
                .requestMatchers(HttpMethod.PUT, "/api/contact/**").hasAnyRole(Role.ADMIN.name(), Role.STAFF.name())
                // Cho phép POST /api/contact mà không cần xác thực (theo yêu cầu)
                .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
                // Tất cả các request khác yêu cầu xác thực
                .anyRequest().authenticated()
        ).csrf(AbstractHttpConfigurer::disable);

        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/oauth2/authorization/google") // Điều hướng đến Google login
                .defaultSuccessUrl("/auth/google/success", true) // Sau khi login thành công
                .failureUrl("/auth/google/failure") // Nếu login thất bại
        );
        // Sử dụng Resource Server JWT
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                        .decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                ).authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );

//        http.sessionManagement(session -> session
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//        );

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role");

        JwtAuthenticationConverter authConverter = new JwtAuthenticationConverter();
        authConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return authConverter;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .openapi("3.0.1")
                .info(new Info()
                        .title("Booking Service API")
                        .version("1.0")
                        .description("Documentation for Booking Service API"))
                .servers(List.of(new Server().url("http://localhost:8080")));
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("http://localhost:[*]");// Chỉ cho phép origin này
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}