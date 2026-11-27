package utils;

import java.util.regex.Pattern;

/**
 * Tiện ích kiểm tra định dạng dữ liệu đầu vào dùng chung cho tầng BUS.
 */
public class ValidationUtil {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static String safeTrim(String value) {
        return value != null ? value.trim() : "";
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
