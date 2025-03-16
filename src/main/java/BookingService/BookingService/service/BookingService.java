package BookingService.BookingService.service;

import BookingService.BookingService.dto.request.BookingRequest;
import BookingService.BookingService.dto.response.BookingResponse;
import BookingService.BookingService.entity.*;
import BookingService.BookingService.enums.BookingStatus;
import BookingService.BookingService.enums.PaymentStatus;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private static final int MAX_SERVICES_PER_BOOKING = 3;
    private static final long MIN_CANCEL_HOURS = 24;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0); // 8:00 AM
    private static final LocalTime CLOSING_TIME = LocalTime.of(20, 0); // 8:00 PM
    private static final int MAX_BOOKING_DAYS_IN_ADVANCE = 7;
    private static final long AUTO_CANCEL_MINUTES = 30;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final ServiceEntityRepository serviceRepository;
    private final ScheduleRepository scheduleRepository;
    private final EmailService emailService;
    private final ScheduleService scheduleService;
    private final NotificationService notificationService;
    @Value("${beautya.feedback.link}")
    private String feedbackLink;
    private final UserService userService;

    // Phương thức chính (CRUD và nghiệp vụ chính)
    public BookingResponse createBooking(BookingRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        User customer = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        List<ServiceEntity> services = serviceRepository.findAllById(request.getServiceIds());
        if (services.isEmpty()) {
            throw new AppException(ErrorCode.SERVICE_NOT_EXISTED);
        }

        if (services.size() > MAX_SERVICES_PER_BOOKING) {
            throw new AppException(ErrorCode.BOOKING_SERVICE_LIMIT_EXCEEDED);
        }

        BigDecimal totalPrice = services.stream()
                .map(ServiceEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalDuration = services.stream().mapToInt(ServiceEntity::getDuration).sum();
        LocalTime startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
        LocalTime endTime = startTime.plusMinutes(totalDuration);
        String timeSlot = request.getStartTime() + "-" + endTime.format(TIME_FORMATTER);

        if (startTime.isBefore(OPENING_TIME) || endTime.isAfter(CLOSING_TIME)) {
            throw new AppException(ErrorCode.TIME_SLOT_OUTSIDE_WORKING_HOURS);
        }

        LocalDateTime bookingDateTime = LocalDateTime.of(request.getBookingDate(), startTime);
        LocalDateTime maxBookingDate = LocalDateTime.now().plusDays(MAX_BOOKING_DAYS_IN_ADVANCE);
        if (bookingDateTime.isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.BOOKING_DATE_IN_PAST);
        }
        if (bookingDateTime.isAfter(maxBookingDate)) {
            throw new AppException(ErrorCode.BOOKING_DATE_TOO_FAR_IN_FUTURE);
        }

        if (bookingRepository.existsByCustomerAndBookingDateAndTimeSlot(customer, request.getBookingDate(), timeSlot)) {
            throw new AppException(ErrorCode.BOOKING_TIME_CONFLICT);
        }

        User specialist;
        if (request.getSpecialistId() != null) {
            specialist = userRepository.findById(request.getSpecialistId())
                    .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));
            if (specialist.getRole() == Role.SPECIALIST && !"ACTIVE".equals(specialist.getStatus())) {
                throw new AppException(ErrorCode.SPECIALIST_NOT_ACTIVE);
            }
            if (!isSpecialistAvailable(specialist.getUserId(), request.getBookingDate(), startTime, endTime)) {
                throw new AppException(ErrorCode.TIME_SLOT_UNAVAILABLE);
            }
        } else {
            specialist = findAvailableSpecialist(request.getBookingDate(), startTime, endTime);
        }

        Schedule schedule = createScheduleForSpecialist(specialist.getUserId(), request.getBookingDate(), timeSlot);

        Booking booking = bookingMapper.toEntity(request);
        bookingMapper.setUserEntities(booking, customer, specialist);

        booking.setServices(services);
        booking.setTotalPrice(totalPrice);
        booking.setTimeSlot(timeSlot);
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        schedule.setAvailability(false);
        scheduleRepository.save(schedule);

        String subject = "Đặt lịch thành công tại Beautya!";
        String htmlBody = buildConfirmationEmail(customer.getName(), specialist.getName(), savedBooking.getBookingDate(), timeSlot, totalPrice);
        emailService.sendEmail(customer.getEmail(), subject, htmlBody);

        notificationService.createWebNotification(customer,
                "Bạn đã đặt lịch thành công vào ngày " + savedBooking.getBookingDate() +
                        ", khung giờ " + savedBooking.getTimeSlot() + " với chuyên viên " + specialist.getName());
        notificationService.notifySpecialistNewBooking(savedBooking);

        return bookingMapper.toResponse(savedBooking);
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<BookingResponse> getBookingById(Long id) {
        return bookingRepository.findById(id).map(bookingMapper::toResponse);
    }

    public List<BookingResponse> getConfirmedOrInProgressBookings() {
        List<BookingStatus> statuses = Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS);
        List<Booking> bookings = bookingRepository.findByStatusIn(statuses);
        return bookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
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

    public BookingResponse updateBooking(Long id, BookingRequest request) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        // Kiểm tra trạng thái: Chỉ cho phép update nếu là PENDING hoặc CONFIRMED
        if (existingBooking.getStatus() != BookingStatus.PENDING && existingBooking.getStatus() != BookingStatus.CONFIRMED) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User customer = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        List<ServiceEntity> services = serviceRepository.findAllById(request.getServiceIds());
        if (services.isEmpty()) {
            throw new AppException(ErrorCode.SERVICE_NOT_EXISTED);
        }

        int totalDuration = services.stream().mapToInt(ServiceEntity::getDuration).sum();
        LocalTime startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
        LocalTime endTime = startTime.plusMinutes(totalDuration);
        String timeSlot = request.getStartTime() + "-" + endTime.format(TIME_FORMATTER);
        if (startTime.isBefore(OPENING_TIME) || endTime.isAfter(CLOSING_TIME)) {
            throw new AppException(ErrorCode.TIME_SLOT_OUTSIDE_WORKING_HOURS);
        }
        LocalDateTime bookingDateTime = LocalDateTime.of(request.getBookingDate(), startTime);
        if (bookingDateTime.isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.BOOKING_DATE_IN_PAST);
        }

        User specialist;
        if (request.getSpecialistId() != null) {
            specialist = userRepository.findById(request.getSpecialistId())
                    .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));
            List<Schedule> schedules = scheduleRepository.findBySpecialistUserIdAndDate(specialist.getUserId(), request.getBookingDate());
            for (Schedule schedule : schedules) {
                LocalTime existingStart = LocalTime.parse(schedule.getTimeSlot().split("-")[0], TIME_FORMATTER);
                LocalTime existingEnd = LocalTime.parse(schedule.getTimeSlot().split("-")[1], TIME_FORMATTER);
                if (isTimeOverlap(startTime, endTime, existingStart, existingEnd) &&
                        !schedule.getTimeSlot().equals(existingBooking.getTimeSlot())) {
                    throw new AppException(ErrorCode.TIME_SLOT_UNAVAILABLE);
                }
            }
        } else {
            specialist = findAvailableSpecialist(request.getBookingDate(), startTime, endTime);
        }

        if (!existingBooking.getBookingDate().equals(request.getBookingDate()) ||
                !existingBooking.getTimeSlot().equals(timeSlot)) {
            if (bookingRepository.existsByCustomerAndBookingDateAndTimeSlotAndBookingIdNot(
                    customer, request.getBookingDate(), timeSlot, id)) {
                throw new AppException(ErrorCode.BOOKING_TIME_CONFLICT);
            }
            Schedule updatedSchedule = validateAndUpdateSchedule(
                    existingBooking, specialist.getUserId(), request.getBookingDate(), startTime, endTime);

            // Xóa lịch cũ, loại trừ booking hiện tại
            if (!existingBooking.getBookingDate().equals(request.getBookingDate()) ||
                    !existingBooking.getTimeSlot().equals(timeSlot)) {
                restorePreviousSchedule(existingBooking.getSpecialist().getUserId(),
                        existingBooking.getBookingDate(), existingBooking.getTimeSlot(), id);
            }
        }
        BigDecimal totalPrice = services.stream()
                .map(ServiceEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        bookingMapper.setUserEntities(existingBooking, customer, specialist);
        existingBooking.setServices(services);
        existingBooking.setBookingDate(request.getBookingDate());
        existingBooking.setTotalPrice(totalPrice);
        existingBooking.setTimeSlot(timeSlot);
        existingBooking.setUpdatedAt(LocalDateTime.now());

        Booking updatedBooking = bookingRepository.save(existingBooking);

        String subject = "Cập nhật đặt lịch tại Beautya";
        String htmlBody = buildUpdateEmail(customer.getName(), specialist.getName(), request.getBookingDate(), timeSlot);
        emailService.sendEmail(customer.getEmail(), subject, htmlBody);

        return bookingMapper.toResponse(updatedBooking);
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
            throw new AppException(ErrorCode.BOOKING_CANNOT_BE_CANCELLED);
        }

        LocalTime startTime = LocalTime.parse(booking.getTimeSlot().split("-")[0], TIME_FORMATTER);
        LocalDateTime bookingStart = LocalDateTime.of(booking.getBookingDate(), startTime);

        long cancelHoursLimit = isHighRole(currentUser.getRole()) ? 0 : 12;
        if (Duration.between(LocalDateTime.now(), bookingStart).toHours() < cancelHoursLimit) {
            throw new AppException(ErrorCode.BOOKING_CANCEL_TIME_EXPIRED);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Xóa lịch, loại trừ booking hiện tại
        restorePreviousSchedule(booking.getSpecialist().getUserId(), booking.getBookingDate(), booking.getTimeSlot(), bookingId);

        String subject = "Xác nhận hủy đặt lịch tại Beautya";
        String htmlBody = buildCancelEmail(booking.getCustomer().getName(), booking.getSpecialist().getName(),
                booking.getBookingDate(), booking.getTimeSlot());
        emailService.sendEmail(booking.getCustomer().getEmail(), subject, htmlBody);
        notificationService.createWebNotification(booking.getCustomer(),
                "Bạn đã hủy lịch hẹn vào ngày " + booking.getBookingDate() +
                        ", khung giờ " + booking.getTimeSlot() + " với chuyên viên " + booking.getSpecialist().getName());

        notificationService.notifySpecialistBookingCancelled(booking);
        return bookingMapper.toResponse(booking);
    }

    private boolean isHighRole(Role role) {
        return role == Role.ADMIN || role == Role.STAFF;
    }

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    public BookingResponse updateBookingStatusByStaff(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
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
        String htmlBody = buildCheckInEmail(booking.getCustomer().getName(), booking.getSpecialist().getName(), booking.getCheckInTime());
        emailService.sendEmail(booking.getCustomer().getEmail(), subject, htmlBody);
        return bookingMapper.toResponse(booking);
    }

    public BookingResponse checkOutBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        if (booking.getCheckInTime() == null) {
            throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
        }

        Payment payment = booking.getPayment();
        if (payment == null) {
            throw new AppException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        if (!PaymentStatus.SUCCESS.equals(payment.getStatus())) {
            throw new AppException(ErrorCode.PAYMENT_NOT_COMPLETED);
        }

        if (!booking.getTotalPrice().equals(payment.getAmount())) {
            throw new AppException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        booking.setPaymentStatus(payment.getStatus());
        booking.setCheckOutTime(LocalDateTime.now());
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        String subject = "Hoàn tất dịch vụ tại Beautya!";
        String htmlBody = buildCheckOutEmail(booking.getCustomer().getName(), booking.getSpecialist().getName(), booking.getCheckOutTime());
        emailService.sendEmail(booking.getCustomer().getEmail(), subject, htmlBody);

        return bookingMapper.toResponse(booking);
    }

    // Phương thức phụ trợ (Helper)
    private User findAvailableSpecialist(LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        List<User> specialists = userRepository.findByRoleAndStatus(Role.SPECIALIST, "ACTIVE");
        if (specialists.isEmpty()) {
            throw new AppException(ErrorCode.NO_AVAILABLE_SPECIALIST);
        }

        for (User specialist : specialists) {
            if (isSpecialistAvailable(specialist.getUserId(), bookingDate, startTime, endTime)) {
                System.out.println("Selected specialist ID: " + specialist.getUserId());
                return specialist;
            }
        }

        throw new AppException(ErrorCode.NO_AVAILABLE_SPECIALIST);
    }

    private boolean isSpecialistAvailable(Long specialistId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        String timeSlot = startTime.format(TIME_FORMATTER) + "-" + endTime.format(TIME_FORMATTER);

        List<Schedule> schedules = scheduleRepository.findBySpecialistUserIdAndDate(specialistId, bookingDate);
        boolean isScheduleConflict = schedules.stream().anyMatch(schedule -> {
            LocalTime existingStart = LocalTime.parse(schedule.getTimeSlot().split("-")[0], TIME_FORMATTER);
            LocalTime existingEnd = LocalTime.parse(schedule.getTimeSlot().split("-")[1], TIME_FORMATTER);
            return isTimeOverlap(startTime, endTime, existingStart, existingEnd);
        });

        boolean isBookingConflict = bookingRepository.existsBySpecialistUserIdAndBookingDateAndTimeSlot(
                specialistId, bookingDate, timeSlot);

        System.out.println("Specialist ID " + specialistId + ": Schedule conflict=" + isScheduleConflict + ", Booking conflict=" + isBookingConflict);
        return !isScheduleConflict && !isBookingConflict;
    }

    private Schedule createScheduleForSpecialist(Long specialistId, LocalDate bookingDate, String timeSlot) {
        User specialist = userRepository.findById(specialistId)
                .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));
        Schedule newSchedule = new Schedule();
        newSchedule.setSpecialist(specialist);
        newSchedule.setDate(bookingDate);
        newSchedule.setTimeSlot(timeSlot);
        newSchedule.setAvailability(true);
        return scheduleRepository.save(newSchedule);
    }

    private Schedule validateAndCreateSchedule(Long specialistId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        String timeSlot = startTime.format(TIME_FORMATTER) + "-" + endTime.format(TIME_FORMATTER);

        User specialist = userRepository.findById(specialistId)
                .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));
        Schedule newSchedule = new Schedule();
        newSchedule.setSpecialist(specialist);
        newSchedule.setDate(bookingDate);
        newSchedule.setTimeSlot(timeSlot);
        newSchedule.setAvailability(true);
        return scheduleRepository.save(newSchedule);
    }

    private Schedule validateAndUpdateSchedule(Booking existingBooking, Long specialistId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        String timeSlot = startTime.format(TIME_FORMATTER) + "-" + endTime.format(TIME_FORMATTER);

        List<Schedule> existingSchedules = scheduleRepository.findBySpecialistUserIdAndDate(specialistId, bookingDate);
        Optional<Schedule> matchingSchedule = existingSchedules.stream()
                .filter(schedule -> schedule.getTimeSlot().equals(timeSlot))
                .findFirst();

        if (matchingSchedule.isPresent()) {
            Schedule schedule = matchingSchedule.get();
            if (!schedule.getAvailability() && !schedule.getTimeSlot().equals(existingBooking.getTimeSlot())) {
                throw new AppException(ErrorCode.TIME_SLOT_UNAVAILABLE);
            }
            schedule.setAvailability(false);
            return scheduleRepository.save(schedule);
        } else {
            User specialist = userRepository.findById(specialistId)
                    .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));
            Schedule newSchedule = new Schedule();
            newSchedule.setSpecialist(specialist);
            newSchedule.setDate(bookingDate);
            newSchedule.setTimeSlot(timeSlot);
            newSchedule.setAvailability(false);
            return scheduleRepository.save(newSchedule);
        }
    }

    private void restorePreviousSchedule(Long specialistId, LocalDate date, String timeSlot, Long excludeBookingId) {
        // Kiểm tra xem có booking nào ở trạng thái PENDING hoặc CONFIRMED (ngoại trừ bookingId được loại trừ) không
        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        boolean bookingExists = bookingRepository.existsBySpecialistUserIdAndBookingDateAndTimeSlotAndStatusInAndBookingIdNot(
                specialistId, date, timeSlot, activeStatuses, excludeBookingId);
        System.out.println("Booking exists (PENDING or CONFIRMED, excluding booking ID " + excludeBookingId + "): " + bookingExists);

        if (!bookingExists) {
            List<Schedule> schedules = scheduleRepository.findBySpecialistUserIdAndDate(specialistId, date);
            Optional<Schedule> oldSchedule = schedules.stream()
                    .filter(schedule -> schedule.getTimeSlot().equals(timeSlot))
                    .findFirst();

            if (oldSchedule.isPresent()) {
                Schedule schedule = oldSchedule.get();
                scheduleRepository.delete(schedule); // Xóa lịch
                System.out.println("Schedule " + schedule.getScheduleId() + " has been deleted");
            } else {
                System.out.println("No schedule found for specialist " + specialistId + " on " + date + " with timeSlot " + timeSlot);
            }
        } else {
            System.out.println("Cannot delete schedule due to existing PENDING or CONFIRMED booking");
        }
    }

    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public User getCurrentUser() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
    }

    public BigDecimal getDailyRevenue(LocalDate date) {
        return bookingRepository.sumTotalPriceByBookingDateAndStatus(
                date != null ? date : LocalDate.now(),
                BookingStatus.COMPLETED
        );
    }

    public BigDecimal getWeeklyRevenue(LocalDate dateInWeek) {
        LocalDate startOfWeek = (dateInWeek != null ? dateInWeek : LocalDate.now())
                .with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return bookingRepository.sumTotalPriceByBookingDateBetweenAndStatus(
                startOfWeek,
                endOfWeek,
                BookingStatus.COMPLETED
        );
    }

    public BigDecimal getMonthlyRevenue(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        return bookingRepository.sumTotalPriceByBookingDateBetweenAndStatus(
                startOfMonth,
                endOfMonth,
                BookingStatus.COMPLETED
        );
    }

    public BigDecimal getRevenueInRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        if (startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        return bookingRepository.sumTotalPriceByBookingDateBetweenAndStatus(
                startDate,
                endDate,
                BookingStatus.COMPLETED
        );
    }

    public List<BookingResponse> getBookingsForCurrentSpecialist() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentSpecialist = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (currentSpecialist.getRole() != Role.SPECIALIST) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<Booking> bookings = bookingRepository.findBySpecialist(currentSpecialist);
        return bookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 300000) // Chạy mỗi 5 phút
    public void autoCancelPendingBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(AUTO_CANCEL_MINUTES);
        List<Booking> pendingBookings = bookingRepository.findByStatusAndCreatedAtBefore(
                BookingStatus.PENDING, threshold);

        for (Booking booking : pendingBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            // Xóa lịch, loại trừ booking hiện tại
            restorePreviousSchedule(booking.getSpecialist().getUserId(),
                    booking.getBookingDate(), booking.getTimeSlot(), booking.getBookingId());

            String subject = "Lịch hẹn của bạn đã bị hủy tự động";
            String htmlBody = buildAutoCancelEmail(booking.getCustomer().getName(),
                    booking.getSpecialist().getName(), booking.getBookingDate(), booking.getTimeSlot());
            emailService.sendEmail(booking.getCustomer().getEmail(), subject, htmlBody);
            notificationService.createWebNotification(booking.getCustomer(),
                    "Lịch hẹn của bạn vào ngày " + booking.getBookingDate() +
                            ", khung giờ " + booking.getTimeSlot() + " đã bị hủy do quá thời gian xác nhận.");

            notificationService.notifySpecialistBookingCancelled(booking);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?") // Chạy lúc 1:00 AM mỗi ngày
    public void autoCancelExpiredBookings() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Booking> expiredBookings = bookingRepository.findByBookingDateBeforeAndStatusIn(
                yesterday, List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS));

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            // Xóa lịch, loại trừ booking hiện tại
            restorePreviousSchedule(booking.getSpecialist().getUserId(),
                    booking.getBookingDate(), booking.getTimeSlot(), booking.getBookingId());

            String subject = "Lịch hẹn của bạn đã tự động bị hủy do hết hạn";
            String htmlBody = buildExpiredBookingEmail(booking.getCustomer().getName(),
                    booking.getSpecialist().getName(), booking.getBookingDate(), booking.getTimeSlot());
            emailService.sendEmail(booking.getCustomer().getEmail(), subject, htmlBody);

            notificationService.createWebNotification(booking.getCustomer(),
                    "Lịch hẹn của bạn vào ngày " + booking.getBookingDate() +
                            ", khung giờ " + booking.getTimeSlot() +
                            " đã tự động bị hủy do đã qua ngày thực hiện.");

            notificationService.notifySpecialistBookingCancelled(booking);
        }
    }

    public BookingResponse createGuestBooking(BookingRequest request) {
        String customerName = request.getCustomerName();
        String customerEmail = request.getCustomerEmail();
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new AppException(ErrorCode.NAME_INVALID);
        }
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_EMAIL);
        }

        User guest = User.builder()
                .email(customerEmail)
                .name(customerName)
                .phone(request.getCustomerPhone() != null ? request.getCustomerPhone() : "N/A")
                .role(Role.GUEST)
                .status("TEMPORARY")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .password(null)
                .build();

        try {
            guest = userService.saveUser(guest);
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.USER_EXISTED) {
                User existingUser = userRepository.findByEmail(customerEmail)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                if (existingUser.getRole() != Role.GUEST) {
                    throw new AppException(ErrorCode.USER_EXISTED);
                }
                guest = existingUser;
            } else {
                throw e;
            }
        }

        List<ServiceEntity> services = serviceRepository.findAllById(request.getServiceIds());
        if (services.isEmpty()) {
            throw new AppException(ErrorCode.SERVICE_NOT_EXISTED);
        }
        if (services.size() > MAX_SERVICES_PER_BOOKING) {
            throw new AppException(ErrorCode.BOOKING_SERVICE_LIMIT_EXCEEDED);
        }

        BigDecimal totalPrice = services.stream()
                .map(ServiceEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalDuration = services.stream().mapToInt(ServiceEntity::getDuration).sum();
        LocalTime startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
        LocalTime endTime = startTime.plusMinutes(totalDuration);
        String timeSlot = request.getStartTime() + "-" + endTime.format(TIME_FORMATTER);

        if (startTime.isBefore(OPENING_TIME) || endTime.isAfter(CLOSING_TIME)) {
            throw new AppException(ErrorCode.TIME_SLOT_OUTSIDE_WORKING_HOURS);
        }

        LocalDateTime bookingDateTime = LocalDateTime.of(request.getBookingDate(), startTime);
        LocalDateTime maxBookingDate = LocalDateTime.now().plusDays(MAX_BOOKING_DAYS_IN_ADVANCE);
        if (bookingDateTime.isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.BOOKING_DATE_IN_PAST);
        }
        if (bookingDateTime.isAfter(maxBookingDate)) {
            throw new AppException(ErrorCode.BOOKING_DATE_TOO_FAR_IN_FUTURE);
        }

        if (bookingRepository.existsByCustomerAndBookingDateAndTimeSlot(guest, request.getBookingDate(), timeSlot)) {
            throw new AppException(ErrorCode.BOOKING_TIME_CONFLICT);
        }

        User specialist;
        if (request.getSpecialistId() != null) {
            specialist = userRepository.findById(request.getSpecialistId())
                    .orElseThrow(() -> new AppException(ErrorCode.SKIN_THERAPIST_NOT_EXISTED));
            if (specialist.getRole() == Role.SPECIALIST && !"ACTIVE".equals(specialist.getStatus())) {
                throw new AppException(ErrorCode.SPECIALIST_NOT_ACTIVE);
            }
            if (!isSpecialistAvailable(specialist.getUserId(), request.getBookingDate(), startTime, endTime)) {
                throw new AppException(ErrorCode.TIME_SLOT_UNAVAILABLE);
            }
        } else {
            specialist = findAvailableSpecialist(request.getBookingDate(), startTime, endTime);
        }

        Schedule schedule = createScheduleForSpecialist(specialist.getUserId(), request.getBookingDate(), timeSlot);

        Booking booking = bookingMapper.toEntity(request);
        bookingMapper.setUserEntities(booking, guest, specialist);

        booking.setServices(services);
        booking.setTotalPrice(totalPrice);
        booking.setTimeSlot(timeSlot);
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        schedule.setAvailability(false);
        scheduleRepository.save(schedule);

        String subject = "Đặt lịch thành công tại Beautya!";
        String htmlBody = buildConfirmationEmail(guest.getName(), specialist.getName(), savedBooking.getBookingDate(), timeSlot, totalPrice);
        emailService.sendEmail(guest.getEmail(), subject, htmlBody);

        notificationService.createWebNotification(guest,
                "Bạn đã đặt lịch thành công vào ngày " + savedBooking.getBookingDate() +
                        ", khung giờ " + savedBooking.getTimeSlot() + " với chuyên viên " + specialist.getName());
        notificationService.notifySpecialistNewBooking(savedBooking);

        return bookingMapper.toResponse(savedBooking);
    }

    @Scheduled(cron = "0 0 1 * * ?") // Chạy lúc 1:00 AM mỗi ngày
    public void autoDeleteTemporaryGuests() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        List<User> expiredGuests = userRepository.findTemporaryGuestsBefore(threshold);

        for (User guest : expiredGuests) {
            if (!userRepository.hasBookings(guest)) {
                userRepository.delete(guest);
            }
        }
    }

    // Các phương thức xây dựng email
    private String buildExpiredBookingEmail(String customerName, String specialistName, LocalDate bookingDate, String timeSlot) {
        return "<!DOCTYPE html>" +
                "<html><head><style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                ".header { background-color: #ff7e9d; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { padding: 20px; background-color: white; border-radius: 0 0 5px 5px; }" +
                ".footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h2>Lịch Hẹn Hết Hạn</h2></div>" +
                "<div class='content'>" +
                "<p>Xin chào " + customerName + ",</p>" +
                "<p>Lịch hẹn của bạn với chuyên viên <strong>" + specialistName + "</strong> " +
                "vào ngày <strong>" + bookingDate + "</strong>, khung giờ <strong>" + timeSlot + "</strong> " +
                "đã tự động bị hủy vì đã qua ngày thực hiện.</p>" +
                "<p>Vui lòng đặt lại lịch nếu bạn vẫn muốn sử dụng dịch vụ!</p>" +
                "</div>" +
                "<div class='footer'>© 2025 Beautya. All rights reserved.</div>" +
                "</div></body></html>";
    }

    private String buildAutoCancelEmail(String customerName, String specialistName, LocalDate bookingDate, String timeSlot) {
        return "<!DOCTYPE html>" +
                "<html><head><style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                ".header { background-color: #ff7e9d; color: white; padding: 10px; text-align: center; }" +
                ".content { padding: 20px; background-color: white; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h2>Lịch Hẹn Hủy Tự Động</h2></div>" +
                "<div class='content'>" +
                "<p>Xin chào " + customerName + ",</p>" +
                "<p>Lịch hẹn của bạn với chuyên viên <strong>" + specialistName + "</strong> " +
                "vào ngày <strong>" + bookingDate + "</strong>, khung giờ <strong>" + timeSlot + "</strong> " +
                "đã bị hủy tự động do không được xác nhận hoặc thanh toán trong vòng " + AUTO_CANCEL_MINUTES + " phút.</p>" +
                "<p>Vui lòng đặt lại lịch nếu cần!</p>" +
                "</div></div></body></html>";
    }

    private String buildConfirmationEmail(String customerName, String specialistName, LocalDate bookingDate, String timeSlot, BigDecimal totalPrice) {
        return "<!DOCTYPE html>" +
                "<html><head><style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                ".header { background-color: #ff7e9d; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { padding: 20px; background-color: white; border-radius: 0 0 5px 5px; }" +
                ".footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h2>Đặt Lịch Thành Công</h2></div>" +
                "<div class='content'>" +
                "<p>Xin chào " + customerName + ",</p>" +
                "<p>Bạn đã đặt lịch thành công với chuyên viên <strong>" + specialistName + "</strong> vào ngày <strong>" + bookingDate + "</strong>, khung giờ <strong>" + timeSlot + "</strong>.</p>" +
                "<p><strong>Tổng giá:</strong> " + totalPrice + " VNĐ</p>" +
                "<p>Chúng tôi rất mong được phục vụ bạn!</p>" +
                "</div>" +
                "<div class='footer'>© 2025 Beautya. All rights reserved.</div>" +
                "</div></body></html>";
    }

    private String buildUpdateEmail(String customerName, String specialistName, LocalDate bookingDate, String timeSlot) {
        return "<!DOCTYPE html>" +
                "<html><head><style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                ".header { background-color: #ff7e9d; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { padding: 20px; background-color: white; border-radius: 0 0 5px 5px; }" +
                ".footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h2>Cập Nhật Lịch Hẹn</h2></div>" +
                "<div class='content'>" +
                "<p>Xin chào " + customerName + ",</p>" +
                "<p>Lịch đặt của bạn với chuyên viên <strong>" + specialistName + "</strong> đã được cập nhật thành công vào ngày <strong>" + bookingDate + "</strong>, khung giờ <strong>" + timeSlot + "</strong>.</p>" +
                "<p>Cảm ơn bạn đã tin tưởng Beautya!</p>" +
                "</div>" +
                "<div class='footer'>© 2025 Beautya. All rights reserved.</div>" +
                "</div></body></html>";
    }

    private String buildCancelEmail(String customerName, String specialistName, LocalDate bookingDate, String timeSlot) {
        return "<!DOCTYPE html>" +
                "<html><head><style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                ".header { background-color: #ff7e9d; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { padding: 20px; background-color: white; border-radius: 0 0 5px 5px; }" +
                ".footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h2>Hủy Lịch Hẹn</h2></div>" +
                "<div class='content'>" +
                "<p>Xin chào " + customerName + ",</p>" +
                "<p>Lịch hẹn của bạn với chuyên viên <strong>" + specialistName + "</strong> vào ngày <strong>" + bookingDate + "</strong>, khung giờ <strong>" + timeSlot + "</strong> đã được hủy thành công.</p>" +
                "<p>Nếu bạn có câu hỏi, vui lòng liên hệ với chúng tôi!</p>" +
                "</div>" +
                "<div class='footer'>© 2025 Beautya. All rights reserved.</div>" +
                "</div></body></html>";
    }

    private String buildCheckInEmail(String customerName, String specialistName, LocalDateTime checkInTime) {
        return "<!DOCTYPE html>" +
                "<html><head><style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                ".header { background-color: #ff7e9d; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { padding: 20px; background-color: white; border-radius: 0 0 5px 5px; }" +
                ".footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h2>Xác Nhận Check-in</h2></div>" +
                "<div class='content'>" +
                "<p>Xin chào " + customerName + ",</p>" +
                "<p>Bạn đã check-in thành công tại Beautya với chuyên viên <strong>" + specialistName + "</strong>.</p>" +
                "<p><strong>Thời gian:</strong> " + checkInTime + "</p>" +
                "<p>Chúc bạn có trải nghiệm tuyệt vời!</p>" +
                "</div>" +
                "<div class='footer'>© 2025 Beautya. All rights reserved.</div>" +
                "</div></body></html>";
    }

    private String buildCheckOutEmail(String customerName, String specialistName, LocalDateTime checkOutTime) {
        return "<!DOCTYPE html>" +
                "<html><head><style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }" +
                ".header { background-color: #ff7e9d; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { padding: 20px; background-color: white; border-radius: 0 0 5px 5px; }" +
                ".footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h2>Hoàn Tất Dịch Vụ</h2></div>" +
                "<div class='content'>" +
                "<p>Xin chào " + customerName + ",</p>" +
                "<p>Bạn đã hoàn tất dịch vụ tại Beautya với chuyên viên <strong>" + specialistName + "</strong>.</p>" +
                "<p><strong>Giờ Check-out:</strong> " + checkOutTime + "</p>" +
                "<p>Cảm ơn bạn đã tin tưởng Beautya! Nếu bạn muốn, hãy để lại phản hồi tại: <a href='" + feedbackLink + "'>Gửi phản hồi</a></p>" +
                "</div>" +
                "<div class='footer'>© 2025 Beautya. All rights reserved.</div>" +
                "</div></body></html>";
    }
}