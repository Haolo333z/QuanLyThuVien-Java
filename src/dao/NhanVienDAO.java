package dao;

import dto.NhanVienDTO;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class NhanVienDAO {

    // Kiểm tra đăng nhập từ bảng NhanVien (chỉ cho phép tài khoản TrangThai = 1)
    public NhanVienDTO checkLogin(String tenDangNhap, String matKhau) {
        String sql = "SELECT * FROM NhanVien WHERE TenDangNhap = ? AND MatKhauHash = ? AND TrangThai = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenDangNhap);
            ps.setString(2, matKhau);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new NhanVienDTO(
                            rs.getInt("MaNV"),
                            rs.getString("HoTen"),
                            rs.getString("TenDangNhap"),
                            rs.getString("MatKhauHash"),
                            rs.getString("VaiTro"),
                            rs.getString("SoDienThoai"),
                            rs.getString("Email"),
                            rs.getBoolean("TrangThai")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}