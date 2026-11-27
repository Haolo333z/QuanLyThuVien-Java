package dao;

import utils.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ThongKeDAO {

    public int getTongSoDauSach() {
        String sql = "SELECT COUNT(*) FROM Sach WHERE TrangThai = 1";
        return querySingleInt(sql);
    }

    public int getTongSoDocGia() {
        String sql = "SELECT COUNT(*) FROM DocGia WHERE TrangThai = 1";
        return querySingleInt(sql);
    }

    public int getSoPhieuDangMuon() {
        String sql = "SELECT COUNT(*) FROM PhieuMuon WHERE TrangThai <> N'Đã trả'";
        return querySingleInt(sql);
    }

    public double getTongTienPhat() {
        String sql = "SELECT ISNULL(SUM(TongTienPhat), 0) FROM PhieuMuon";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTienPhatDuKienQuaHan(long phatTreMoiNgay) {
        if (phatTreMoiNgay <= 0) {
            return 0;
        }
        String sql = "SELECT ISNULL(SUM(CAST(DATEDIFF(DAY, HanTra, CAST(GETDATE() AS DATE)) AS DECIMAL(18, 2)) * ?), 0) "
                   + "FROM PhieuMuon "
                   + "WHERE TrangThai <> N'Đã trả' AND HanTra < CAST(GETDATE() AS DATE)";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, phatTreMoiNgay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getSoPhieuMuonTheoKy(LocalDate tuNgay, LocalDate denNgay) {
        String sql = "SELECT COUNT(*) FROM PhieuMuon WHERE NgayMuon >= ? AND NgayMuon <= ?";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getSoPhieuDaTraTheoKy(LocalDate tuNgay, LocalDate denNgay) {
        String sql = "SELECT COUNT(*) FROM PhieuMuon WHERE TrangThai = N'Đã trả' AND NgayTraThucTe >= ? AND NgayTraThucTe <= ?";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTongTienPhatTheoKy(LocalDate tuNgay, LocalDate denNgay) {
        String sql = "SELECT ISNULL(SUM(TongTienPhat), 0) FROM PhieuMuon "
                   + "WHERE TrangThai = N'Đã trả' AND NgayTraThucTe >= ? AND NgayTraThucTe <= ?";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Object[]> getThongKeTheoThang(LocalDate tuNgay, LocalDate denNgay) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT CONVERT(VARCHAR(7), NgayMuon, 120) AS ThangNam, "
                   + "       COUNT(*) AS SoPhieuMuon, "
                   + "       SUM(CASE WHEN TrangThai = N'Đã trả' THEN 1 ELSE 0 END) AS SoPhieuDaTra, "
                   + "       ISNULL(SUM(TongTienPhat), 0) AS TongTienPhat "
                   + "FROM PhieuMuon "
                   + "WHERE NgayMuon >= ? AND NgayMuon <= ? "
                   + "GROUP BY CONVERT(VARCHAR(7), NgayMuon, 120) "
                   + "ORDER BY ThangNam ASC";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getString("ThangNam"),
                            rs.getInt("SoPhieuMuon"),
                            rs.getInt("SoPhieuDaTra"),
                            String.format("%,.0f", rs.getDouble("TongTienPhat"))
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> getThongKeTheoTheLoai() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT t.TenTheLoai, "
                   + "       COUNT(DISTINCT s.MaSach) AS SoDauSach, "
                   + "       ISNULL(SUM(s.SoLuongTong), 0) AS TongBanIn, "
                   + "       ISNULL(SUM(s.SoLuongConLai), 0) AS ConLai, "
                   + "       ISNULL(SUM(ISNULL(s.SoLuongThanhLy, 0)), 0) AS DaThanhLy, "
                   + "       ISNULL((SELECT SUM(ct.SoLuong) "
                   + "               FROM ChiTietPhieuMuon ct "
                   + "               JOIN Sach s2 ON ct.MaSach = s2.MaSach "
                   + "               WHERE s2.MaTheLoai = t.MaTheLoai), 0) AS TongLuotMuon "
                   + "FROM TheLoai t "
                   + "LEFT JOIN Sach s ON t.MaTheLoai = s.MaTheLoai "
                   + "GROUP BY t.MaTheLoai, t.TenTheLoai "
                   + "ORDER BY TongLuotMuon DESC, t.TenTheLoai ASC";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("TenTheLoai"),
                        rs.getInt("SoDauSach"),
                        rs.getInt("TongBanIn"),
                        rs.getInt("ConLai"),
                        rs.getInt("DaThanhLy"),
                        rs.getInt("TongLuotMuon")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> getTopSachMuonNhieu() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT TOP 10 s.MaSach, s.TenSach, s.TacGia, SUM(ct.SoLuong) AS TongLuotMuon "
                   + "FROM ChiTietPhieuMuon ct "
                   + "JOIN Sach s ON ct.MaSach = s.MaSach "
                   + "GROUP BY s.MaSach, s.TenSach, s.TacGia "
                   + "ORDER BY TongLuotMuon DESC";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("MaSach"),
                        rs.getString("TenSach"),
                        rs.getString("TacGia"),
                        rs.getInt("TongLuotMuon")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> getDanhSachQuaHan() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT pm.MaPhieuMuon, dg.HoTen, dg.SoDienThoai, pm.NgayMuon, pm.HanTra, "
                   + "DATEDIFF(DAY, pm.HanTra, CAST(GETDATE() AS DATE)) AS SoNgayTre "
                   + "FROM PhieuMuon pm "
                   + "JOIN DocGia dg ON pm.MaDocGia = dg.MaDocGia "
                   + "WHERE pm.TrangThai <> N'Đã trả' AND pm.HanTra < CAST(GETDATE() AS DATE) "
                   + "ORDER BY SoNgayTre DESC";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("MaPhieuMuon"),
                        rs.getString("HoTen"),
                        rs.getString("SoDienThoai"),
                        rs.getDate("NgayMuon").toString(),
                        rs.getDate("HanTra").toString(),
                        rs.getInt("SoNgayTre") + " ngày"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private int querySingleInt(String sql) {
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}