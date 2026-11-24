package dao;

import dto.SachDTO;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SachDAO {

    public List<SachDTO> getAll() {
        List<SachDTO> list = new ArrayList<>();
        String sql = "SELECT s.*, t.TenTheLoai FROM Sach s "
                   + "JOIN TheLoai t ON s.MaTheLoai = t.MaTheLoai "
                   + "ORDER BY s.MaSach DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<SachDTO> search(String keyword, int maTheLoai) {
        List<SachDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT s.*, t.TenTheLoai FROM Sach s "
              + "JOIN TheLoai t ON s.MaTheLoai = t.MaTheLoai WHERE 1=1 ");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (s.TenSach LIKE ? OR s.TacGia LIKE ? OR s.ISBN LIKE ?) ");
        }
        if (maTheLoai > 0) {
            sql.append("AND s.MaTheLoai = ? ");
        }
        sql.append("ORDER BY s.MaSach DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim() + "%";
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
            }
            if (maTheLoai > 0) {
                ps.setInt(idx, maTheLoai);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean isIsbnExists(String isbn, int excludeMaSach) {
        if (isbn == null || isbn.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM Sach WHERE ISBN = ? AND MaSach <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn.trim());
            ps.setInt(2, excludeMaSach);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insert(SachDTO s) {
        String sql = "INSERT INTO Sach (MaTheLoai, TenSach, TacGia, NhaXuatBan, NamXuatBan, ISBN, SoLuongTong, SoLuongConLai, ViTriKe, TrangThai) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getMaTheLoai());
            ps.setString(2, s.getTenSach());
            ps.setString(3, s.getTacGia());
            ps.setString(4, s.getNhaXuatBan());
            ps.setInt(5, s.getNamXuatBan());
            ps.setString(6, s.getIsbn().isEmpty() ? null : s.getIsbn());
            ps.setInt(7, s.getSoLuongTong());
            ps.setInt(8, s.getSoLuongConLai());
            ps.setString(9, s.getViTriKe());
            ps.setBoolean(10, s.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(SachDTO s) {
        String sql = "UPDATE Sach SET MaTheLoai = ?, TenSach = ?, TacGia = ?, NhaXuatBan = ?, NamXuatBan = ?, "
                   + "ISBN = ?, SoLuongTong = ?, SoLuongConLai = ?, ViTriKe = ?, TrangThai = ? WHERE MaSach = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getMaTheLoai());
            ps.setString(2, s.getTenSach());
            ps.setString(3, s.getTacGia());
            ps.setString(4, s.getNhaXuatBan());
            ps.setInt(5, s.getNamXuatBan());
            ps.setString(6, s.getIsbn().isEmpty() ? null : s.getIsbn());
            ps.setInt(7, s.getSoLuongTong());
            ps.setInt(8, s.getSoLuongConLai());
            ps.setString(9, s.getViTriKe());
            ps.setBoolean(10, s.isTrangThai());
            ps.setInt(11, s.getMaSach());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateTrangThai(int maSach, boolean trangThai) {
        String sql = "UPDATE Sach SET TrangThai = ? WHERE MaSach = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, trangThai);
            ps.setInt(2, maSach);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private SachDTO mapRow(ResultSet rs) throws Exception {
        return new SachDTO(
                rs.getInt("MaSach"),
                rs.getInt("MaTheLoai"),
                rs.getString("TenTheLoai"),
                rs.getString("TenSach"),
                rs.getString("TacGia"),
                rs.getString("NhaXuatBan"),
                rs.getInt("NamXuatBan"),
                rs.getString("ISBN"),
                rs.getInt("SoLuongTong"),
                rs.getInt("SoLuongConLai"),
                rs.getString("ViTriKe"),
                rs.getBoolean("TrangThai")
        );
    }
}