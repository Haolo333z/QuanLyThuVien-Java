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
        try (Connection conn = DBConnection.requireConnection();
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
        try (Connection conn = DBConnection.requireConnection();
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
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int soLanGiaHanIdx = -1;
            try {
                soLanGiaHanIdx = rs.findColumn("SoLanGiaHan");
            } catch (SQLException ignored) {
            }

            LocalDate today = LocalDate.now();
            while (rs.next()) {
                Date ngayTra = rs.getDate("NgayTraThucTe");
                int soLanGiaHan = soLanGiaHanIdx > 0 ? rs.getInt(soLanGiaHanIdx) : 0;
                LocalDate hanTra = rs.getDate("HanTra").toLocalDate();
                String trangThai = rs.getString("TrangThai");
                if (!"Đã trả".equalsIgnoreCase(trangThai) && hanTra.isBefore(today)) {
                    trangThai = "Quá hạn";
                }
                list.add(new PhieuMuonDTO(
                        rs.getInt("MaPhieuMuon"),
                        rs.getInt("MaDocGia"),
                        rs.getString("TenDocGia"),
                        rs.getInt("MaNV"),
                        rs.getString("TenNV"),
                        rs.getDate("NgayMuon").toLocalDate(),
                        hanTra,
                        ngayTra != null ? ngayTra.toLocalDate() : null,
                        trangThai,
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
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sqlWithCount)) {
            ps.setDate(1, Date.valueOf(hanTraMoi));
            ps.setInt(2, maPhieuMuon);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Fallback nếu bảng PhieuMuon chưa chạy ALTER thêm cột SoLanGiaHan
            String sqlFallback = "UPDATE PhieuMuon SET HanTra = ? WHERE MaPhieuMuon = ? AND TrangThai <> N'Đã trả'";
            try (Connection conn = DBConnection.requireConnection();
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
        try (Connection conn = DBConnection.requireConnection();
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
        if (dsSach == null || dsSach.isEmpty()) {
            return false;
        }
        String sqlPhieu = "INSERT INTO PhieuMuon (MaDocGia, MaNV, NgayMuon, HanTra, TrangThai, TongTienPhat) "
                        + "VALUES (?, ?, CAST(GETDATE() AS DATE), ?, N'Đang mượn', 0)";
        String sqlChiTiet = "INSERT INTO ChiTietPhieuMuon (MaPhieuMuon, MaSach, SoLuong, TinhTrangKhiMuon) VALUES (?, ?, ?, N'Bình thường')";
        String sqlTruKho = "UPDATE Sach SET SoLuongConLai = SoLuongConLai - ? WHERE MaSach = ? AND TrangThai = 1 AND SoLuongConLai >= ?";

        Connection conn = null;
        try {
            conn = DBConnection.requireConnection();
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
                    // Trừ tồn kho trước, nếu không đủ tồn kho hoặc sách đã ẩn thì rollback
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
                    if (psCT.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
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

    // TRANSACTION TRẢ SÁCH: Cập nhật PhieuMuon (chống trả trùng) + ChiTietPhieuMuon + Cập nhật kho
    public boolean traSachTransaction(int maPhieuMuon, double tongTienPhat, List<ChiTietPhieuMuonDTO> dsTra) {
        if (dsTra == null || dsTra.isEmpty()) {
            return false;
        }
        String sqlPhieu = "UPDATE PhieuMuon SET NgayTraThucTe = CAST(GETDATE() AS DATE), TrangThai = N'Đã trả', TongTienPhat = ? "
                        + "WHERE MaPhieuMuon = ? AND TrangThai <> N'Đã trả'";
        String sqlCT = "UPDATE ChiTietPhieuMuon SET TinhTrangKhiTra = ?, TienPhat = ? WHERE MaChiTiet = ?";
        String sqlCongKho = "UPDATE Sach SET SoLuongConLai = SoLuongConLai + ? WHERE MaSach = ? AND SoLuongConLai + ? <= SoLuongTong";
        String sqlMatSach = "UPDATE Sach SET SoLuongTong = SoLuongTong - ?, "
                          + "TrangThai = CASE WHEN SoLuongConLai <= 0 AND SoLuongTong - ? <= 0 THEN 0 ELSE TrangThai END "
                          + "WHERE MaSach = ? AND SoLuongTong - ? >= SoLuongConLai";

        Connection conn = null;
        try {
            conn = DBConnection.requireConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psPhieu = conn.prepareStatement(sqlPhieu)) {
                psPhieu.setDouble(1, tongTienPhat);
                psPhieu.setInt(2, maPhieuMuon);
                if (psPhieu.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement psCT = conn.prepareStatement(sqlCT);
                 PreparedStatement psKho = conn.prepareStatement(sqlCongKho);
                 PreparedStatement psMat = conn.prepareStatement(sqlMatSach)) {
                for (ChiTietPhieuMuonDTO ct : dsTra) {
                    psCT.setString(1, ct.getTinhTrangKhiTra());
                    psCT.setDouble(2, ct.getTienPhat());
                    psCT.setInt(3, ct.getMaChiTiet());
                    if (psCT.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }

                    if (!"Mất sách".equalsIgnoreCase(ct.getTinhTrangKhiTra())) {
                        psKho.setInt(1, ct.getSoLuong());
                        psKho.setInt(2, ct.getMaSach());
                        psKho.setInt(3, ct.getSoLuong());
                        if (psKho.executeUpdate() == 0) {
                            conn.rollback();
                            return false;
                        }
                    } else {
                        psMat.setInt(1, ct.getSoLuong());
                        psMat.setInt(2, ct.getSoLuong());
                        psMat.setInt(3, ct.getMaSach());
                        psMat.setInt(4, ct.getSoLuong());
                        if (psMat.executeUpdate() == 0) {
                            conn.rollback();
                            return false;
                        }
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