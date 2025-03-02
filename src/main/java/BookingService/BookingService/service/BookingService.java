    package BookingService.BookingService.service;

    import BookingService.BookingService.dto.request.BookingRequest;
    import BookingService.BookingService.dto.response.BookingResponse;
    import BookingService.BookingService.entity.Booking;
    import BookingService.BookingService.entity.Schedule;
    import BookingService.BookingService.entity.ServiceEntity;
    import BookingService.BookingService.entity.User;
    import BookingService.BookingService.enums.BookingStatus;
    import BookingService.BookingService.enums.Role;
    import BookingService.BookingService.exception.AppException;
    import BookingService.BookingService.exception.ErrorCode;
    import BookingService.BookingService.mapper.BookingMapper;
    import BookingService.BookingService.repository.BookingRepository;
    import BookingService.BookingService.repository.ScheduleRepository;
    import BookingService.BookingService.repository.ServiceEntityRepository;
    import BookingService.BookingService.repository.UserRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.math.BigDecimal;
    import java.time.Duration;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.time.LocalTime;
    import java.time.format.DateTimeFormatter;
    import java.util.List;
    import java.util.Optional;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    @Transactional
    public class BookingService {

        private final BookingRepository bookingRepository;
        private final UserRepository userRepository;
        private final BookingMapper bookingMapper;
        private final ServiceEntityRepository serviceRepository;
        private final ScheduleRepository scheduleRepository;
        private final EmailService emailService;
        private final ScheduleService scheduleService;

        @Value("${beautya.feedback.link}")
        private String feedbackLink;

        private static final int MAX_SERVICES_PER_BOOKING = 3; // Giới hạn số dịch vụ tối đa
        private static final long MIN_CANCEL_HOURS = 24; // Giờ tối thiểu để hủy

        public List<BookingResponse> getAllBookings() {
            return bookingRepository.findAll().stream()
                    .map(bookingMapper::toResponse)
                    .collect(Collectors.toList());
        }

        public Optional<BookingResponse> getBookingById(Long id) {
            return bookingRepository.findById(id).map(bookingMapper::toResponse);
        }

        public BookingResponse createBooking(BookingRequest request) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();

            User customer = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

            User specialist = userRepository.findById(request.getSpecialistId())
                    .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));

            List<ServiceEntity> services = serviceRepository.findAllById(request.getServiceIds());
            if (services.isEmpty()) {
                throw new AppException(ErrorCode.SERVICE_NOT_EXISTED);
            }

            // Business Rule 1: Kiểm tra số lượng dịch vụ tối đa
            if (services.size() > MAX_SERVICES_PER_BOOKING) {
                throw new AppException(ErrorCode.BOOKING_SERVICE_LIMIT_EXCEEDED);
            }

            BigDecimal totalPrice = services.stream()
                    .map(ServiceEntity::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Business Rule 2: Kiểm tra ngày đặt lịch phải trong tương lai
            LocalDateTime bookingDateTime = LocalDateTime.of(request.getBookingDate(), parseTimeSlotStart(request.getTimeSlot()));
            if (bookingDateTime.isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.BOOKING_DATE_IN_PAST);
            }

            // Business Rule 3: Kiểm tra tổng thời gian dịch vụ so với khung giờ
            int totalDuration = services.stream().mapToInt(ServiceEntity::getDuration).sum();
            if (!isDurationValidForTimeSlot(totalDuration, request.getTimeSlot())) {
                throw new AppException(ErrorCode.BOOKING_DURATION_EXCEEDS_TIME_SLOT);
            }

            // Business Rule 4: Kiểm tra trùng lịch của khách hàng
            if (bookingRepository.existsByCustomerAndBookingDateAndTimeSlot(customer, request.getBookingDate(), request.getTimeSlot())) {
                throw new AppException(ErrorCode.BOOKING_TIME_CONFLICT);
            }

            // Kiểm tra lịch của specialist
            validateSchedule(request.getSpecialistId(), request.getBookingDate(), request.getTimeSlot());

            Booking booking = bookingMapper.toEntity(request);
            bookingMapper.setUserEntities(booking, customer, specialist);

            booking.setTotalPrice(totalPrice);
            booking.setStatus(BookingStatus.PENDING);
            booking.setCreatedAt(LocalDateTime.now());
            booking.setUpdatedAt(LocalDateTime.now());

            Booking savedBooking = bookingRepository.save(booking);

            // Cập nhật availability của Schedule
            updateScheduleAvailability(request.getSpecialistId(), request.getBookingDate(), request.getTimeSlot());

            // Gửi email xác nhận
            String subject = "Đặt lịch thành công tại Beautya!";
            String htmlBody = "<!DOCTYPE html>" +
                    "<html><head><style>" +
                    "body { font-family: Arial, sans-serif; color: #333; }" +
                    ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                    ".header { background-color: #ff7e9d; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                    ".content { padding: 20px; background-color: white; border-radius: 0 0 5px 5px; }" +
                    ".footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }" +
                    ".icon { width: 50px; height: 50px; }" +
                    "</style></head><body>" +
                    "<div class='container'>" +
                    "<div class='header'>" +
                    "<img src='https://i.ibb.co/F46xfC1B/1.jpg' alt='Skin Care Image' class='email-image'>" +
                    "<h2>Đặt Lịch Thành Công</h2></div>" +
                    "<div class='content'>" +
                    "<p>Xin chào " + customer.getName() + ",</p>" +
                    "<p>Bạn đã đặt lịch thành công với chuyên viên <strong>" + specialist.getName() + "</strong> vào ngày <strong>" + savedBooking.getBookingDate() + "</strong>, khung giờ <strong>" + request.getTimeSlot() + "</strong>.</p>" +
                    "<p><strong>Tổng giá:</strong> " + totalPrice + " VNĐ</p>" +
                    "<p>Chúng tôi rất mong được phục vụ bạn!</p>" +
                    "</div>" +
                    "<div class='footer'>© 2025 Beautya. All rights reserved.</div>" +
                    "</div></body></html>";

            emailService.sendEmail(customer.getEmail(), subject, htmlBody);

            return bookingMapper.toResponse(savedBooking);
        }

        private void validateSchedule(Long specialistId, LocalDate bookingDate, String timeSlot) {
            if (timeSlot == null || !timeSlot.matches("\\d{2}:\\d{2}-\\d{2}:\\d{2}")) {
                throw new AppException(ErrorCode.INVALID_TIME_SLOT_FORMAT);
            }

            List<Schedule> schedules = scheduleService.getSchedulesBySpecialistAndDate(specialistId, bookingDate);
            Optional<Schedule> matchingSchedule = schedules.stream()
                    .filter(schedule -> schedule.getTimeSlot().equals(timeSlot))
                    .findFirst();

            if (matchingSchedule.isEmpty()) {
                throw new AppException(ErrorCode.SCHEDULE_NOT_FOUND);
            }

            Schedule schedule = matchingSchedule.get();
            if (!schedule.getAvailability()) {
                throw new AppException(ErrorCode.SCHEDULE_NOT_AVAILABLE);
            }

            if (bookingRepository.existsBySpecialistUserIdAndBookingDateAndTimeSlot(specialistId, bookingDate, timeSlot)) {
                throw new AppException(ErrorCode.TIME_SLOT_UNAVAILABLE);
            }
        }

        private void updateScheduleAvailability(Long specialistId, LocalDate bookingDate, String timeSlot) {
            List<Schedule> schedules = scheduleService.getSchedulesBySpecialistAndDate(specialistId, bookingDate);
            Optional<Schedule> matchingSchedule = schedules.stream()
                    .filter(schedule -> schedule.getTimeSlot().equals(timeSlot))
                    .findFirst();
            if (matchingSchedule.isPresent()) {
                Schedule schedule = matchingSchedule.get();
                schedule.setAvailability(false);
                scheduleRepository.save(schedule);
            }
        }

        private LocalTime parseTimeSlotStart(String timeSlot) {
            String[] parts = timeSlot.split("-");
            return LocalTime.parse(parts[0], DateTimeFormatter.ofPattern("HH:mm"));
        }

        private boolean isDurationValidForTimeSlot(int totalDuration, String timeSlot) {
            String[] parts = timeSlot.split("-");
            LocalTime start = LocalTime.parse(parts[0], DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime end = LocalTime.parse(parts[1], DateTimeFormatter.ofPattern("HH:mm"));
            long slotMinutes = Duration.between(start, end).toMinutes();
            return totalDuration <= slotMinutes;
        }

        public BookingResponse updateBooking(Long id, BookingRequest request) {
            Booking existingBooking = bookingRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

            var authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();
            User customer = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

            User specialist = userRepository.findById(request.getSpecialistId())
                    .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));

            // Business Rule 2 & 4: Áp dụng lại khi cập nhật
            LocalDateTime bookingDateTime = LocalDateTime.of(request.getBookingDate(), parseTimeSlotStart(request.getTimeSlot()));
            if (bookingDateTime.isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.BOOKING_DATE_IN_PAST);
            }

            if (!existingBooking.getBookingDate().equals(request.getBookingDate()) ||
                    !existingBooking.getTimeSlot().equals(request.getTimeSlot())) {
                if (bookingRepository.existsByCustomerAndBookingDateAndTimeSlot(customer, request.getBookingDate(), request.getTimeSlot())) {
                    throw new AppException(ErrorCode.BOOKING_TIME_CONFLICT);
                }
                validateSchedule(request.getSpecialistId(), request.getBookingDate(), request.getTimeSlot());
            }

            bookingMapper.setUserEntities(existingBooking, customer, specialist);
            existingBooking.setBookingDate(request.getBookingDate());
            existingBooking.setTimeSlot(request.getTimeSlot());
            existingBooking.setUpdatedAt(LocalDateTime.now());

            Booking updatedBooking = bookingRepository.save(existingBooking);

            String subject = "Cập nhật đặt lịch tại Beautya";
            String htmlBody = "<!DOCTYPE html>" +
                    "<html><head><style>" +
                    "body { font-family: Arial, sans-serif; color: #333; }" +
                    ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                    ".header { background-color: #ff7e9d; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                    ".content { padding: 20px; background-color: white; border-radius: 0 0 5px 5px; }" +
                    ".footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }" +
                    ".icon { width: 50px; height: 50px; }" +
                    "</style></head><body>" +
                    "<div class='container'>" +
                    "<div class='header'>" +
                    "<img src='https://i.ibb.co/F46xfC1B/1.jpg' alt='Skin Care Image' class='email-image'>" +
                    "<h2>Cập Nhật Lịch Hẹn</h2></div>" +
                    "<div class='content'>" +
                    "<p>Xin chào " + customer.getName() + ",</p>" +
                    "<p>Lịch đặt của bạn với chuyên viên <strong>" + specialist.getName() + "</strong> đã được cập nhật thành công vào ngày <strong>" + request.getBookingDate() + "</strong>, khung giờ <strong>" + request.getTimeSlot() + "</strong>.</p>" +
                    "<p>Cảm ơn bạn đã tin tưởng Beautya!</p>" +
                    "</div>" +
                    "<div class='footer'>© 2025 Beautya. All rights reserved.</div>" +
                    "</div></body></html>";

            emailService.sendEmail(customer.getEmail(), subject, htmlBody);

            return bookingMapper.toResponse(updatedBooking);
        }

        public void deleteBooking(Long id) {
            bookingRepository.deleteById(id);
        }

        public BookingResponse cancelBookingByUser(Long bookingId) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

            if (!isHighRole(currentUser.getRole()) && !booking.getCustomer().getEmail().equalsIgnoreCase(currentUserEmail)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            if (booking.getStatus() != BookingStatus.PENDING) {
                throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
            }

            // Business Rule 3: Kiểm tra thời gian hủy
            LocalDateTime bookingStart = LocalDateTime.of(booking.getBookingDate(), parseTimeSlotStart(booking.getTimeSlot()));
            if (Duration.between(LocalDateTime.now(), bookingStart).toHours() < MIN_CANCEL_HOURS) {
                throw new AppException(ErrorCode.BOOKING_CANCEL_TIME_EXPIRED);
            }

            booking.setStatus(BookingStatus.CANCELLED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            String subject = "Xác nhận hủy đặt lịch tại Beautya";
            String htmlBody = "<!DOCTYPE html>" +
                    "<html><head><style>" +
                    "body { font-family: Arial, sans-serif; color: #333; }" +
                    ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                    ".header { background-color: #ff7e9d; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                    ".content { padding: 20px; background-color: white; border-radius: 0 0 5px 5px; }" +
                    ".footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }" +
                    ".icon { width: 50px; height: 50px; }" +
                    "</style></head><body>" +
                    "<div class='container'>" +
                    "<div class='header'>" +
                    "<img src='https://i.ibb.co/F46xfC1B/1.jpg' alt='Skin Care Image' class='email-image'>" +
                    "<h2>Hủy Lịch Hẹn</h2></div>" +
                    "<div class='content'>" +
                    "<p>Xin chào " + booking.getCustomer().getName() + ",</p>" +
                    "<p>Lịch hẹn của bạn với chuyên viên <strong>" + booking.getSpecialist().getName() + "</strong> vào ngày <strong>" + booking.getBookingDate() + "</strong>, khung giờ <strong>" + booking.getTimeSlot() + "</strong> đã được hủy thành công.</p>" +
                    "<p>Nếu bạn có câu hỏi, vui lòng liên hệ với chúng tôi!</p>" +
                    "</div>" +
                    "<div class='footer'>© 2025 Beautya. All rights reserved.</div>" +
                    "</div></body></html>";

            emailService.sendEmail(booking.getCustomer().getEmail(), subject, htmlBody);

            return bookingMapper.toResponse(booking);
        }

        public BookingResponse updateBookingStatusByStaff(Long bookingId, BookingStatus newStatus) {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

            booking.setStatus(newStatus);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            return bookingMapper.toResponse(booking);
        }

        public BookingResponse checkInBooking(Long bookingId) {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
            }

            booking.setCheckInTime(LocalDateTime.now());
            booking.setStatus(BookingStatus.IN_PROGRESS);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            String subject = "Xác nhận Check-in tại Beautya";
            String htmlBody = "<!DOCTYPE html>" +
                    "<html><head><style>" +
                    "body { font-family: Arial, sans-serif; color: #333; }" +
                    ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                    ".header { background-color: #ff7e9d; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                    ".content { padding: 20px; background-color: white; border-radius: 0 0 5px 5px; }" +
                    ".footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }" +
                    ".icon { width: 50px; height: 50px; }" +
                    "</style></head><body>" +
                    "<div class='container'>" +
                    "<div class='header'>" +
                    "<img src='https://i.ibb.co/F46xfC1B/1.jpg' alt='Skin Care Image' class='email-image'>" +
                    "<h2>Xác Nhận Check-in</h2></div>" +
                    "<div class='content'>" +
                    "<p>Xin chào " + booking.getCustomer().getName() + ",</p>" +
                    "<p>Bạn đã check-in thành công tại Beautya với chuyên viên <strong>" + booking.getSpecialist().getName() + "</strong>.</p>" +
                    "<p><strong>Thời gian:</strong> " + booking.getCheckInTime() + "</p>" +
                    "<p>Chúc bạn có trải nghiệm tuyệt vời!</p>" +
                    "</div>" +
                    "<div class='footer'>© 2025 Beautya. All rights reserved.</div>" +
                    "</div></body></html>";

            emailService.sendEmail(booking.getCustomer().getEmail(), subject, htmlBody);
            return bookingMapper.toResponse(booking);
        }

        public BookingResponse checkOutBooking(Long bookingId) {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

            if (booking.getCheckInTime() == null) {
                throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
            }

            booking.setCheckOutTime(LocalDateTime.now());
            booking.setStatus(BookingStatus.COMPLETED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            String subject = "Hoàn tất dịch vụ tại Beautya!";
            String htmlBody = "<!DOCTYPE html>" +
                    "<html><head><style>" +
                    "body { font-family: Arial, sans-serif; color: #333; }" +
                    ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                    ".header { background-color: #ff7e9d; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                    ".content { padding: 20px; background-color: white; border-radius: 0 0 5px 5px; }" +
                    ".footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }" +
                    ".icon { width: 50px; height: 50px; }" +
                    "</style></head><body>" +
                    "<div class='container'>" +
                    "<div class='header'>" +
                    "<img src='https://i.ibb.co/F46xfC1B/1.jpg' alt='Skin Care Image' class='email-image'>" +
                    "<h2>Hoàn Tất Dịch Vụ</h2></div>" +
                    "<div class='content'>" +
                    "<p>Xin chào " + booking.getCustomer().getName() + ",</p>" +
                    "<p>Bạn đã hoàn tất dịch vụ tại Beautya với chuyên viên <strong>" + booking.getSpecialist().getName() + "</strong>.</p>" +
                    "<p><strong>Giờ Check-out:</strong> " + booking.getCheckOutTime() + "</p>" +
                    "<p>Cảm ơn bạn đã tin tưởng Beautya! Nếu bạn muốn, hãy để lại phản hồi tại: <a href='" + feedbackLink + "'>Gửi phản hồi</a></p>" +
                    "</div>" +
                    "<div class='footer'>© 2025 Beautya. All rights reserved.</div>" +
                    "</div></body></html>";

            emailService.sendEmail(booking.getCustomer().getEmail(), subject, htmlBody);

            return bookingMapper.toResponse(booking);
        }

        public List<BookingResponse> getBookingsForCurrentUser() {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

            List<Booking> bookings = bookingRepository.findByCustomer(currentUser);
            return bookings.stream()
                    .map(bookingMapper::toResponse)
                    .collect(Collectors.toList());
        }

        private boolean isHighRole(Role role) {
            return role == Role.ADMIN || role == Role.STAFF || role == Role.SPECIALIST;
        }
    }