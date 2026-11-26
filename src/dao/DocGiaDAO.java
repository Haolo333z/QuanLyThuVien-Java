package dao;

import dto.DocGiaDTO;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocGiaDAO {

    public List<DocGiaDTO> getAll() {
        List<DocGiaDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM DocGia ORDER BY MaDocGia DESC";
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

    public List<DocGiaDTO> search(String keyword) {
        List<DocGiaDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM DocGia WHERE HoTen LIKE ? OR SoDienThoai LIKE ? OR Email LIKE ? ORDER BY MaDocGia DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
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

    public DocGiaDTO getById(int maDocGia) {
        String sql = "SELECT * FROM DocGia WHERE MaDocGia = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDocGia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public DocGiaDTO findByPhoneOrEmail(String sdt, String email) {
        String cleanPhone = sdt != null ? sdt.trim() : "";
        String cleanEmail = email != null ? email.trim() : "";

        boolean hasPhone = !cleanPhone.isEmpty();
        boolean hasEmail = !cleanEmail.isEmpty();

        if (!hasPhone && !hasEmail) {
            return null;
        }

        StringBuilder sql = new StringBuilder("SELECT TOP 1 * FROM DocGia WHERE ");
        if (hasPhone && hasEmail) {
            sql.append("(SoDienThoai = ? OR LOWER(Email) = LOWER(?))");
        } else if (hasPhone) {
            sql.append("SoDienThoai = ?");
        } else {
            sql.append("LOWER(Email) = LOWER(?)");
        }
        sql.append(" ORDER BY TrangThai DESC, MaDocGia DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (hasPhone && hasEmail) {
                ps.setString(1, cleanPhone);
                ps.setString(2, cleanEmail);
            } else if (hasPhone) {
                ps.setString(1, cleanPhone);
            } else {
                ps.setString(1, cleanEmail);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(DocGiaDTO dg) {
        String sql = "INSERT INTO DocGia (HoTen, NgaySinh, GioiTinh, SoDienThoai, Email, DiaChi, NgayLapThe, NgayHetHan, TrangThai) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dg.getHoTen());
            if (dg.getNgaySinh() != null) ps.setDate(2, Date.valueOf(dg.getNgaySinh()));
            else ps.setNull(2, Types.DATE);
            ps.setString(3, dg.getGioiTinh());
            ps.setString(4, dg.getSoDienThoai());
            ps.setString(5, dg.getEmail());
            ps.setString(6, dg.getDiaChi());
            ps.setDate(7, Date.valueOf(dg.getNgayLapThe()));
            ps.setDate(8, Date.valueOf(dg.getNgayHetHan()));
            ps.setBoolean(9, dg.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(DocGiaDTO dg) {
        String sql = "UPDATE DocGia SET HoTen = ?, NgaySinh = ?, GioiTinh = ?, SoDienThoai = ?, Email = ?, DiaChi = ?, TrangThai = ? "
                   + "WHERE MaDocGia = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dg.getHoTen());
            if (dg.getNgaySinh() != null) ps.setDate(2, Date.valueOf(dg.getNgaySinh()));
            else ps.setNull(2, Types.DATE);
            ps.setString(3, dg.getGioiTinh());
            ps.setString(4, dg.getSoDienThoai());
            ps.setString(5, dg.getEmail());
            ps.setString(6, dg.getDiaChi());
            ps.setBoolean(7, dg.isTrangThai());
            ps.setInt(8, dg.getMaDocGia());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean giaHanThe(int maDocGia, LocalDate ngayHetHanMoi) {
        String sql = "UPDATE DocGia SET NgayHetHan = ? WHERE MaDocGia = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ngayHetHanMoi));
            ps.setInt(2, maDocGia);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateTrangThai(int maDocGia, boolean trangThai) {
        String sql = "UPDATE DocGia SET TrangThai = ? WHERE MaDocGia = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, trangThai);
            ps.setInt(2, maDocGia);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private DocGiaDTO mapRow(ResultSet rs) throws Exception {
        Date ns = rs.getDate("NgaySinh");
        return new DocGiaDTO(
                rs.getInt("MaDocGia"),
                rs.getString("HoTen"),
                ns != null ? ns.toLocalDate() : null,
                rs.getString("GioiTinh"),
                rs.getString("SoDienThoai"),
                rs.getString("Email"),
                rs.getString("DiaChi"),
                rs.getDate("NgayLapThe").toLocalDate(),
                rs.getDate("NgayHetHan").toLocalDate(),
                rs.getBoolean("TrangThai")
        );
    }
}