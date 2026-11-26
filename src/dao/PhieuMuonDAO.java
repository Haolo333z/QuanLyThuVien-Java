package dao;

import dto.ChiTietPhieuMuonDTO;
import dto.PhieuMuonDTO;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhieuMuonDAO {

    // Đếm tổng số cuốn sách độc giả đang mượn (chưa trả)
    public int countSachDangMuon(int maDocGia) {
        String sql = "SELECT ISNULL(SUM(ct.SoLuong), 0) "
                   + "FROM PhieuMuon pm JOIN ChiTietPhieuMuon ct ON pm.MaPhieuMuon = ct.MaPhieuMuon "
                   + "WHERE pm.MaDocGia = ? AND pm.TrangThai <> N'Đã trả'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDocGia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Kiểm tra độc giả có phiếu mượn nào đang quá hạn không
    public boolean hasPhieuQuaHan(int maDocGia) {
        String sql = "SELECT 1 FROM PhieuMuon WHERE MaDocGia = ? AND TrangThai <> N'Đã trả' AND HanTra < CAST(GETDATE() AS DATE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDocGia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy danh sách phiếu mượn (nếu chiDangMuon = true thì chỉ lấy phiếu chưa trả)
    public List<PhieuMuonDTO> getAll(boolean chiDangMuon) {
        List<PhieuMuonDTO> list = new ArrayList<>();
        String sql = "SELECT pm.*, dg.HoTen AS TenDocGia, nv.HoTen AS TenNV "
                   + "FROM PhieuMuon pm "
                   + "JOIN DocGia dg ON pm.MaDocGia = dg.MaDocGia "
                   + "JOIN NhanVien nv ON pm.MaNV = nv.MaNV "
                   + (chiDangMuon ? "WHERE pm.TrangThai <> N'Đã trả' " : "")
                   + "ORDER BY pm.MaPhieuMuon DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int soLanGiaHanIdx = -1;
            try {
                soLanGiaHanIdx = rs.findColumn("SoLanGiaHan");
            } catch (SQLException ignored) {
            }

            while (rs.next()) {
                Date ngayTra = rs.getDate("NgayTraThucTe");
                int soLanGiaHan = soLanGiaHanIdx > 0 ? rs.getInt(soLanGiaHanIdx) : 0;
                list.add(new PhieuMuonDTO(
                        rs.getInt("MaPhieuMuon"),
                        rs.getInt("MaDocGia"),
                        rs.getString("TenDocGia"),
                        rs.getInt("MaNV"),
                        rs.getString("TenNV"),
                        rs.getDate("NgayMuon").toLocalDate(),
                        rs.getDate("HanTra").toLocalDate(),
                        ngayTra != null ? ngayTra.toLocalDate() : null,
                        rs.getString("TrangThai"),
                        rs.getDouble("TongTienPhat"),
                        soLanGiaHan
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Gia hạn thời gian trả cho phiếu mượn đang hoạt động
    public boolean giaHanPhieuMuon(int maPhieuMuon, LocalDate hanTraMoi) {
        String sqlWithCount = "UPDATE PhieuMuon SET HanTra = ?, SoLanGiaHan = ISNULL(SoLanGiaHan, 0) + 1 "
                            + "WHERE MaPhieuMuon = ? AND TrangThai <> N'Đã trả'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlWithCount)) {
            ps.setDate(1, Date.valueOf(hanTraMoi));
            ps.setInt(2, maPhieuMuon);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Fallback nếu bảng PhieuMuon chưa chạy ALTER thêm cột SoLanGiaHan
            String sqlFallback = "UPDATE PhieuMuon SET HanTra = ? WHERE MaPhieuMuon = ? AND TrangThai <> N'Đã trả'";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlFallback)) {
                ps.setDate(1, Date.valueOf(hanTraMoi));
                ps.setInt(2, maPhieuMuon);
                return ps.executeUpdate() > 0;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy chi tiết sách của 1 phiếu mượn
    public List<ChiTietPhieuMuonDTO> getChiTietByPhieu(int maPhieuMuon) {
        List<ChiTietPhieuMuonDTO> list = new ArrayList<>();
        String sql = "SELECT ct.*, s.TenSach FROM ChiTietPhieuMuon ct "
                   + "JOIN Sach s ON ct.MaSach = s.MaSach "
                   + "WHERE ct.MaPhieuMuon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPhieuMuon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ChiTietPhieuMuonDTO(
                            rs.getInt("MaChiTiet"),
                            rs.getInt("MaPhieuMuon"),
                            rs.getInt("MaSach"),
                            rs.getString("TenSach"),
                            rs.getInt("SoLuong"),
                            rs.getString("TinhTrangKhiMuon"),
                            rs.getString("TinhTrangKhiTra"),
                            rs.getDouble("TienPhat")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // TRANSACTION LẬP PHIẾU MƯỢN: Tạo PhieuMuon + ChiTietPhieuMuon + Trừ SoLuongConLai
    public boolean lapPhieuMuonTransaction(int maDocGia, int maNV, LocalDate hanTra, List<ChiTietPhieuMuonDTO> dsSach) {
        String sqlPhieu = "INSERT INTO PhieuMuon (MaDocGia, MaNV, NgayMuon, HanTra, TrangThai, TongTienPhat) "
                        + "VALUES (?, ?, CAST(GETDATE() AS DATE), ?, N'Đang mượn', 0)";
        String sqlChiTiet = "INSERT INTO ChiTietPhieuMuon (MaPhieuMuon, MaSach, SoLuong, TinhTrangKhiMuon) VALUES (?, ?, ?, N'Bình thường')";
        String sqlTruKho = "UPDATE Sach SET SoLuongConLai = SoLuongConLai - ? WHERE MaSach = ? AND SoLuongConLai >= ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            int maPhieuMoi = -1;
            try (PreparedStatement psPhieu = conn.prepareStatement(sqlPhieu, Statement.RETURN_GENERATED_KEYS)) {
                psPhieu.setInt(1, maDocGia);
                psPhieu.setInt(2, maNV);
                psPhieu.setDate(3, Date.valueOf(hanTra));
                psPhieu.executeUpdate();
                try (ResultSet rs = psPhieu.getGeneratedKeys()) {
                    if (rs.next()) maPhieuMoi = rs.getInt(1);
                }
            }

            if (maPhieuMoi == -1) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement psCT = conn.prepareStatement(sqlChiTiet);
                 PreparedStatement psKho = conn.prepareStatement(sqlTruKho)) {
                for (ChiTietPhieuMuonDTO ct : dsSach) {
                    // Trừ tồn kho trước, nếu không đủ tồn kho thì rollback
                    psKho.setInt(1, ct.getSoLuong());
                    psKho.setInt(2, ct.getMaSach());
                    psKho.setInt(3, ct.getSoLuong());
                    int updated = psKho.executeUpdate();
                    if (updated == 0) {
                        conn.rollback();
                        return false;
                    }

                    // Ghi chi tiết phiếu mượn
                    psCT.setInt(1, maPhieuMoi);
                    psCT.setInt(2, ct.getMaSach());
                    psCT.setInt(3, ct.getSoLuong());
                    psCT.executeUpdate();
                }
            }

            conn.commit(); // Thành công -> Commit
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    // TRANSACTION TRẢ SÁCH: Cập nhật PhieuMuon + ChiTietPhieuMuon + Cộng lại kho (trừ sách bị mất)
    public boolean traSachTransaction(int maPhieuMuon, double tongTienPhat, List<ChiTietPhieuMuonDTO> dsTra) {
        String sqlPhieu = "UPDATE PhieuMuon SET NgayTraThucTe = CAST(GETDATE() AS DATE), TrangThai = N'Đã trả', TongTienPhat = ? WHERE MaPhieuMuon = ?";
        String sqlCT = "UPDATE ChiTietPhieuMuon SET TinhTrangKhiTra = ?, TienPhat = ? WHERE MaChiTiet = ?";
        String sqlCongKho = "UPDATE Sach SET SoLuongConLai = SoLuongConLai + ? WHERE MaSach = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psPhieu = conn.prepareStatement(sqlPhieu)) {
                psPhieu.setDouble(1, tongTienPhat);
                psPhieu.setInt(2, maPhieuMuon);
                psPhieu.executeUpdate();
            }

            try (PreparedStatement psCT = conn.prepareStatement(sqlCT);
                 PreparedStatement psKho = conn.prepareStatement(sqlCongKho)) {
                for (ChiTietPhieuMuonDTO ct : dsTra) {
                    psCT.setString(1, ct.getTinhTrangKhiTra());
                    psCT.setDouble(2, ct.getTienPhat());
                    psCT.setInt(3, ct.getMaChiTiet());
                    psCT.executeUpdate();

                    // Chỉ cộng lại tồn kho nếu sách không bị mất
                    if (!"Mất sách".equalsIgnoreCase(ct.getTinhTrangKhiTra())) {
                        psKho.setInt(1, ct.getSoLuong());
                        psKho.setInt(2, ct.getMaSach());
                        psKho.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
}