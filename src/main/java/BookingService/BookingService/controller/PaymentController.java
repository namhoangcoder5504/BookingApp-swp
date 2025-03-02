//package BookingService.BookingService.controller;
//
//import BookingService.BookingService.configuration.PaymentConfig;
//import BookingService.BookingService.entity.Booking;
//import BookingService.BookingService.entity.Payment;
//import BookingService.BookingService.enums.PaymentStatus;
//import BookingService.BookingService.repository.BookingRepository;
//import BookingService.BookingService.repository.PaymentRepository;
//import BookingService.BookingService.service.PaymentService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//@RestController
//@RequestMapping("/api/payment")
//public class PaymentController {
//
//    @Autowired
//    BookingRepository bookingRepository;
//
//    @Autowired
//    PaymentService paymentService;
//
//    @Autowired
//    PaymentRepository paymentRepository;
//
//
//    /**
//     * 1) Tạo Payment (PENDING)
//     * 2) Build URL VNPAY
//     * 3) Return link để FE redirect
//     */
//    @GetMapping("/pay")
//    public String payVnpay(@RequestParam Long bookingId) throws UnsupportedEncodingException {
//        // Lấy booking từ DB
//        Booking booking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new RuntimeException("Booking not found"));
//
//        // Tạo Payment record với trạng thái PENDING
//        Payment payment = paymentService.createPayment(booking, "VNPAY");
//
//        // Generate vnp_TxnRef (lưu vào Payment.transactionId)
//        String vnp_TxnRef = PaymentConfig.getRandomNumber(8);
//        payment.setTransactionId(vnp_TxnRef);
//        paymentRepository.save(payment);
//
//        // THÔNG TIN CƠ BẢN
//        String vnp_Version = "2.1.0";
//        String vnp_Command = "pay";
//        String orderType = "other"; // loại dịch vụ
//        long amount = booking.getTotalPrice().longValue() * 100;
//        // VNPAY quy ước: Nạp số tiền * 100 (VD: 10000 => 1000000)
//
//        // Build params
//        Map<String, String> vnp_Params = new HashMap<>();
//        vnp_Params.put("vnp_Version", vnp_Version);
//        vnp_Params.put("vnp_Command", vnp_Command);
//        vnp_Params.put("vnp_TmnCode", PaymentConfig.vnp_TmnCode);
//        vnp_Params.put("vnp_Amount", String.valueOf(amount));
//        vnp_Params.put("vnp_CurrCode", "VND");
//        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
//        vnp_Params.put("vnp_OrderInfo", "Thanh toan bookingId=" + bookingId);
//        vnp_Params.put("vnp_OrderType", orderType);
//        vnp_Params.put("vnp_Locale", "vn");
//
//        // ReturnUrl: URL mà user sẽ được redirect sau khi thanh toán (frontend, hoặc 1 trang tạm)
//        // Trong demo, ta chưa triển khai, có thể đặt tạm:
//        String returnUrl = "http://localhost:8080/api/payment/vnpay-return";
//        vnp_Params.put("vnp_ReturnUrl", returnUrl);
//
//        // IP user (nếu cần):
//        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
//
//        // Thời gian tạo
//        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//        String vnp_CreateDate = formatter.format(cld.getTime());
//        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
//
//        // Thời gian hết hạn (thêm 15p)
//        cld.add(Calendar.MINUTE, 15);
//        String vnp_ExpireDate = formatter.format(cld.getTime());
//        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
//
//        // Bước 2. Tạo URL chứa các tham số + vnp_SecureHash
//        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
//        Collections.sort(fieldNames);
//        StringBuilder hashData = new StringBuilder();
//        StringBuilder query = new StringBuilder();
//        for (int i = 0; i < fieldNames.size(); i++) {
//            String name = fieldNames.get(i);
//            String value = vnp_Params.get(name);
//            // Build hashData
//            hashData.append(name).append("=").append(URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()));
//            // Build query
//            query.append(URLEncoder.encode(name, StandardCharsets.US_ASCII.toString()))
//                    .append("=")
//                    .append(URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()));
//            if (i < fieldNames.size() - 1) {
//                hashData.append("&");
//                query.append("&");
//            }
//        }
//
//        // Tạo vnp_SecureHash
//        String vnp_SecureHash = PaymentConfig.hmacSHA512(PaymentConfig.secretKey, hashData.toString());
//        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
//
//        String paymentUrl = PaymentConfig.vnp_PayUrl + "?" + query.toString();
//
//        // Trả về URL để FE redirect người dùng sang màn hình thanh toán VNPAY
//        return paymentUrl;
//    }
//
//
//    /**
//     * 2) Handler khi VNPAY redirect người dùng về returnUrl
//     *    (Nếu bạn muốn hiển thị trang confirm, check success/fail)
//     */
//    @GetMapping("/vnpay-return")
//    public String vnpayReturn(
//            @RequestParam Map<String, String> allParams
//    ) {
//        // Ở đây, VNPAY sẽ redirect user + query params
//        // Tùy bạn hiển thị trang thank-you hoặc kiểm tra
//        String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
//        String vnp_TxnRef = allParams.get("vnp_TxnRef"); // TransactionId ta đã gán
//        // Kiểm tra vnp_SecureHash => validate (nếu cần)
//        // ...
//        if ("00".equals(vnp_ResponseCode)) {
//            // Giao dịch thành công
//            return "Thanh toán thành công! Mã GD: " + vnp_TxnRef;
//        } else {
//            return "Thanh toán không thành công hoặc bị hủy!";
//        }
//    }
//
//
//    /**
//     * 3) Endpoint để VNPAY gọi IPN (server->server) thông báo kết quả thanh toán
//     *    => Merchant update Payment + Booking
//     */
//    @PostMapping("/vnpay-ipn")
//    public String vnpayIpn(@RequestParam Map<String, String> allParams) {
//        // Lấy vnp_TxnRef => transactionId cục bộ
//        // Lấy vnp_ResponseCode => "00" = success
//        // Check vnp_SecureHash => confirm request từ VNPAY
//        String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
//        String vnp_TxnRef = allParams.get("vnp_TxnRef");
//
//        // Giả sử ta đã check hash OK
//        PaymentStatus finalStatus = "00".equals(vnp_ResponseCode)
//                ? PaymentStatus.SUCCESS
//                : PaymentStatus.FAILED;
//        // Cập nhật Payment
//        paymentService.updatePaymentStatus(vnp_TxnRef, finalStatus);
//
//        // VNPAY yêu cầu trả về "OK" hoặc 1 chuỗi
//        return "OK";
//    }
//
//
//    /**
//     * 4) Endpoint Test: Kiểm tra Payment trong DB (JSON)
//     */
//    @GetMapping("/testPayment")
//    public Payment testPayment(@RequestParam String txnRef) {
//        return paymentRepository.findByTransactionId(txnRef)
//                .orElse(null);
//    }
//}
