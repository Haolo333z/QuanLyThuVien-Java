package dao;

import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Object[]> getTopSachMuonNhieu() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT TOP 10 s.MaSach, s.TenSach, s.TacGia, SUM(ct.SoLuong) AS TongLuotMuon "
                   + "FROM ChiTietPhieuMuon ct "
                   + "JOIN Sach s ON ct.MaSach = s.MaSach "
                   + "GROUP BY s.MaSach, s.TenSach, s.TacGia "
                   + "ORDER BY TongLuotMuon DESC";
        try (Connection conn = DBConnection.getConnection();
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
        try (Connection conn = DBConnection.getConnection();
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
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}