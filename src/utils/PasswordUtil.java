package utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Tiện ích băm và xác thực mật khẩu bằng PBKDF2WithHmacSHA256.
 * Hỗ trợ nhận diện mật khẩu dạng plaintext cũ để tự động nâng cấp khi đăng nhập.
 */
public class PasswordUtil {
    private static final String PREFIX = "pbkdf2$";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String hashPassword(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Mật khẩu không được null");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(rawPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        return PREFIX + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        if (!storedHash.startsWith(PREFIX)) {
            // Tương thích dữ liệu cũ lưu plaintext trước khi áp dụng hash
            return MessageDigest.isEqual(
                    rawPassword.getBytes(StandardCharsets.UTF_8),
                    storedHash.getBytes(StandardCharsets.UTF_8)
            );
        }

        String[] parts = storedHash.split("\\$");
        if (parts.length != 4) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            byte[] actualHash = pbkdf2(rawPassword.toCharArray(), salt, iterations, expectedHash.length * 8);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean needsRehash(String storedHash) {
        return storedHash == null || !storedHash.startsWith(PREFIX);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Không thể băm mật khẩu PBKDF2", e);
        } finally {
            spec.clearPassword();
        }
    }
}
