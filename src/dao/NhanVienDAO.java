package dao;

import dto.NhanVienDTO;
import utils.DBConnection;
import utils.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    public NhanVienDTO findActiveByUsername(String tenDangNhap) {
        String sql = "SELECT * FROM NhanVien WHERE TenDangNhap = ? AND TrangThai = 1";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
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

    public NhanVienDTO checkLogin(String tenDangNhap, String matKhau) {
        NhanVienDTO nv = findActiveByUsername(tenDangNhap);
        if (nv != null && PasswordUtil.verifyPassword(matKhau, nv.getMatKhauHash())) {
            return nv;
        }
        return null;
    }

    public NhanVienDTO getById(int maNV) {
        String sql = "SELECT * FROM NhanVien WHERE MaNV = ?";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
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

    public int countActiveAdmins() {
        String sql = "SELECT COUNT(*) FROM NhanVien WHERE VaiTro = N'Admin' AND TrangThai = 1";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<NhanVienDTO> getAll() {
        List<NhanVienDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien ORDER BY MaNV DESC";
        try (Connection conn = DBConnection.requireConnection();
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

    public List<NhanVienDTO> search(String keyword) {
        List<NhanVienDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien WHERE HoTen LIKE ? OR TenDangNhap LIKE ? OR SoDienThoai LIKE ? ORDER BY MaNV DESC";
        try (Connection conn = DBConnection.requireConnection();
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

    public boolean isUsernameExists(String tenDangNhap) {
        String sql = "SELECT 1 FROM NhanVien WHERE TenDangNhap = ?";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insert(NhanVienDTO nv) {
        String sql = "INSERT INTO NhanVien (HoTen, TenDangNhap, MatKhauHash, VaiTro, SoDienThoai, Email, TrangThai) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getHoTen());
            ps.setString(2, nv.getTenDangNhap());
            ps.setString(3, nv.getMatKhauHash());
            ps.setString(4, nv.getVaiTro());
            ps.setString(5, nv.getSoDienThoai());
            ps.setString(6, nv.getEmail());
            ps.setBoolean(7, nv.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(NhanVienDTO nv) {
        String sql = "UPDATE NhanVien SET HoTen = ?, MatKhauHash = ?, VaiTro = ?, SoDienThoai = ?, Email = ?, TrangThai = ? "
                   + "WHERE MaNV = ?";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getHoTen());
            ps.setString(2, nv.getMatKhauHash());
            ps.setString(3, nv.getVaiTro());
            ps.setString(4, nv.getSoDienThoai());
            ps.setString(5, nv.getEmail());
            ps.setBoolean(6, nv.isTrangThai());
            ps.setInt(7, nv.getMaNV());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateWithoutPassword(NhanVienDTO nv) {
        String sql = "UPDATE NhanVien SET HoTen = ?, VaiTro = ?, SoDienThoai = ?, Email = ?, TrangThai = ? "
                   + "WHERE MaNV = ?";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getHoTen());
            ps.setString(2, nv.getVaiTro());
            ps.setString(3, nv.getSoDienThoai());
            ps.setString(4, nv.getEmail());
            ps.setBoolean(5, nv.isTrangThai());
            ps.setInt(6, nv.getMaNV());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateMatKhauHash(int maNV, String matKhauHash) {
        String sql = "UPDATE NhanVien SET MatKhauHash = ? WHERE MaNV = ?";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauHash);
            ps.setInt(2, maNV);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateTrangThai(int maNV, boolean trangThai) {
        String sql = "UPDATE NhanVien SET TrangThai = ? WHERE MaNV = ?";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, trangThai);
            ps.setInt(2, maNV);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private NhanVienDTO mapRow(ResultSet rs) throws Exception {
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