package utils;

// Lưu ý: Khi Phúc merge NhanVienDTO lên main, đổi Object thành dto.NhanVienDTO
public class Session {
    public static Object currentUser = null;

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }
}