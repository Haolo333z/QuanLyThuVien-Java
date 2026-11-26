package utils;

import dto.NhanVienDTO;

public class Session {
    public static NhanVienDTO currentUser = null;

    public static void setCurrentUser(NhanVienDTO user) {
        currentUser = user;
    }

    public static NhanVienDTO getCurrentUser() {
        return currentUser;
    }

    public static int getCurrentMaNV() {
        return currentUser != null ? currentUser.getMaNV() : 0;
    }

    public static boolean isAdmin() {
        return currentUser != null && "Admin".equalsIgnoreCase(currentUser.getVaiTro());
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }
}