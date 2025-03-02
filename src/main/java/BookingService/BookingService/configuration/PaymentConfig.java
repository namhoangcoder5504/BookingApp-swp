//package BookingService.BookingService.configuration;
//
//import jakarta.servlet.http.HttpServletRequest;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//
//public class PaymentConfig {
//    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
//    public static String vnp_TmnCode = "HC2GZLAE"; // Terminal ID
//    public static String secretKey = "Y10JCFMO86K6E4OZNZ6GPAH6AXV0W241";
//    public static String vnp_ApiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
//
//    // Tạo ngẫu nhiên chuỗi (VD cho mã giao dịch cục bộ)
//    public static String getRandomNumber(int len) {
//        Random rnd = new Random();
//        String chars = "0123456789";
//        StringBuilder sb = new StringBuilder(len);
//        for (int i = 0; i < len; i++) {
//            sb.append(chars.charAt(rnd.nextInt(chars.length())));
//        }
//        return sb.toString();
//    }
//
//    // Tạo chữ ký HmacSHA512
//    public static String hmacSHA512(final String key, final String data) {
//        try {
//            if (key == null || data == null) {
//                throw new NullPointerException();
//            }
//            final Mac hmac512 = Mac.getInstance("HmacSHA512");
//            byte[] hmacKeyBytes = key.getBytes();
//            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
//            hmac512.init(secretKey);
//            byte[] dataBytes = data.getBytes(StandardCharsets.US_ASCII);
//            byte[] result = hmac512.doFinal(dataBytes);
//            StringBuilder sb = new StringBuilder(2 * result.length);
//            for (byte b : result) {
//                sb.append(String.format("%02x", b & 0xff));
//            }
//            return sb.toString();
//        } catch (Exception ex) {
//            return "";
//        }
//    }
//
//    // Hàm build chuỗi query + mã hóa để tạo vnp_SecureHash
//    public static String hashAllFields(Map<String, String> fields) {
//        List<String> fieldNames = new ArrayList<>(fields.keySet());
//        Collections.sort(fieldNames);
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < fieldNames.size(); i++) {
//            String fieldName = fieldNames.get(i);
//            String fieldValue = fields.get(fieldName);
//            if ((fieldValue != null) && (fieldValue.length() > 0)) {
//                sb.append(fieldName);
//                sb.append("=");
//                sb.append(fieldValue);
//                if (i < (fieldNames.size() - 1)) {
//                    sb.append("&");
//                }
//            }
//        }
//        return hmacSHA512(secretKey, sb.toString());
//    }
//
//    // Lấy IP Client (nếu cần)
//    public static String getIpAddress(HttpServletRequest request) {
//        String ipAdress;
//        try {
//            ipAdress = request.getHeader("X-FORWARDED-FOR");
//            if (ipAdress == null) {
//                ipAdress = request.getRemoteAddr();
//            }
//        } catch (Exception e) {
//            ipAdress = "Invalid IP:" + e.getMessage();
//        }
//        return ipAdress;
//    }
//}