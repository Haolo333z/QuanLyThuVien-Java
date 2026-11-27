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
        return searchPhieuMuon("", chiDangMuon ? "Chưa trả" : "Tất cả");
    }

    // Tìm kiếm & lọc danh sách phiếu mượn theo từ khóa (Mã phiếu, Tên ĐG, SĐT ĐG) và trạng thái
    public List<PhieuMuonDTO> searchPhieuMuon(String keyword, String trangThaiFilter) {
        List<PhieuMuonDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT pm.*, dg.HoTen AS TenDocGia, nv.HoTen AS TenNV "
              + "FROM PhieuMuon pm "
              + "JOIN DocGia dg ON pm.MaDocGia = dg.MaDocGia "
              + "JOIN NhanVien nv ON pm.MaNV = nv.MaNV "
              + "WHERE 1=1 ");

        String cleanKey = keyword != null ? keyword.trim() : "";
        if (!cleanKey.isEmpty()) {
            sql.append("AND (CAST(pm.MaPhieuMuon AS VARCHAR(20)) LIKE ? OR dg.HoTen LIKE ? OR dg.SoDienThoai LIKE ?) ");
        }

        String filter = trangThaiFilter != null ? trangThaiFilter.trim() : "Tất cả";
        if ("Chưa trả".equalsIgnoreCase(filter)) {
            sql.append("AND pm.TrangThai <> N'Đã trả' ");
        } else if ("Đang mượn".equalsIgnoreCase(filter)) {
            sql.append("AND pm.TrangThai <> N'Đã trả' AND pm.HanTra >= CAST(GETDATE() AS DATE) ");
        } else if ("Quá hạn".equalsIgnoreCase(filter)) {
            sql.append("AND pm.TrangThai <> N'Đã trả' AND pm.HanTra < CAST(GETDATE() AS DATE) ");
        } else if ("Đã trả".equalsIgnoreCase(filter)) {
            sql.append("AND pm.TrangThai = N'Đã trả' ");
        }

        sql.append("ORDER BY pm.MaPhieuMuon DESC");

        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (!cleanKey.isEmpty()) {
                String pattern = "%" + cleanKey + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
                ps.setString(3, pattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                int soLanGiaHanIdx = findColumnSafe(rs, "SoLanGiaHan");
                LocalDate today = LocalDate.now();
                while (rs.next()) {
                    list.add(mapPhieuMuonRow(rs, soLanGiaHanIdx, today));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private int findColumnSafe(ResultSet rs, String columnName) {
        try {
            return rs.findColumn(columnName);
        } catch (SQLException ignored) {
            return -1;
        }
    }

    private PhieuMuonDTO mapPhieuMuonRow(ResultSet rs, int soLanGiaHanIdx, LocalDate today) throws SQLException {
        Date ngayTra = rs.getDate("NgayTraThucTe");
        int soLanGiaHan = soLanGiaHanIdx > 0 ? rs.getInt(soLanGiaHanIdx) : 0;
        LocalDate hanTra = rs.getDate("HanTra").toLocalDate();
        String trangThai = rs.getString("TrangThai");
        if (!"Đã trả".equalsIgnoreCase(trangThai) && hanTra.isBefore(today)) {
            trangThai = "Quá hạn";
        }
        return new PhieuMuonDTO(
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
        );
    }

    public PhieuMuonDTO getById(int maPhieuMuon) {
        String sql = "SELECT pm.*, dg.HoTen AS TenDocGia, nv.HoTen AS TenNV "
                   + "FROM PhieuMuon pm "
                   + "JOIN DocGia dg ON pm.MaDocGia = dg.MaDocGia "
                   + "JOIN NhanVien nv ON pm.MaNV = nv.MaNV "
                   + "WHERE pm.MaPhieuMuon = ?";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPhieuMuon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int soLanGiaHanIdx = findColumnSafe(rs, "SoLanGiaHan");
                    return mapPhieuMuonRow(rs, soLanGiaHanIdx, LocalDate.now());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        return lapPhieuMuonTransactionReturnId(maDocGia, maNV, hanTra, dsSach) > 0;
    }

    public int lapPhieuMuonTransactionReturnId(int maDocGia, int maNV, LocalDate hanTra, List<ChiTietPhieuMuonDTO> dsSach) {
        if (dsSach == null || dsSach.isEmpty()) {
            return -1;
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
                return -1;
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
                        return -1;
                    }

                    // Ghi chi tiết phiếu mượn
                    psCT.setInt(1, maPhieuMoi);
                    psCT.setInt(2, ct.getMaSach());
                    psCT.setInt(3, ct.getSoLuong());
                    if (psCT.executeUpdate() == 0) {
                        conn.rollback();
                        return -1;
                    }

                    // Đánh dấu đã nhận nếu độc giả có đặt trước sách này
                    DatTruocDAO.markReaderReservationsReceived(conn, maDocGia, ct.getMaSach());
                }
            }

            conn.commit(); // Thành công -> Commit
            return maPhieuMoi;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return -1;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    // TRANSACTION TRẢ SÁCH: Cập nhật PhieuMuon (chống trả trùng) + ChiTietPhieuMuon + Cập nhật kho + Ghi log mất sách + Đẩy hàng đợi đặt trước
    public boolean traSachTransaction(int maPhieuMuon, double tongTienPhat, List<ChiTietPhieuMuonDTO> dsTra) {
        return traSachTransaction(maPhieuMuon, tongTienPhat, dsTra, utils.Session.getCurrentMaNV());
    }

    public boolean traSachTransaction(int maPhieuMuon, double tongTienPhat, List<ChiTietPhieuMuonDTO> dsTra, int maNV) {
        return traSachTransactionWithNotifications(maPhieuMuon, tongTienPhat, dsTra, maNV) != null;
    }

    public List<String> traSachTransactionWithNotifications(int maPhieuMuon, double tongTienPhat,
                                                            List<ChiTietPhieuMuonDTO> dsTra, int maNV) {
        if (dsTra == null || dsTra.isEmpty()) {
            return null;
        }
        String sqlPhieu = "UPDATE PhieuMuon SET NgayTraThucTe = CAST(GETDATE() AS DATE), TrangThai = N'Đã trả', TongTienPhat = ? "
                        + "WHERE MaPhieuMuon = ? AND TrangThai <> N'Đã trả'";
        String sqlCT = "UPDATE ChiTietPhieuMuon SET TinhTrangKhiTra = ?, TienPhat = ? WHERE MaChiTiet = ?";
        String sqlCongKho = "UPDATE Sach SET SoLuongConLai = SoLuongConLai + ? WHERE MaSach = ? AND SoLuongConLai + ? <= SoLuongTong";
        String sqlMatSach = "UPDATE Sach SET SoLuongTong = SoLuongTong - ?, "
                          + "TrangThai = CASE WHEN SoLuongConLai <= 0 AND SoLuongTong - ? <= 0 THEN 0 ELSE TrangThai END "
                          + "WHERE MaSach = ? AND SoLuongTong - ? >= SoLuongConLai";

        List<String> reservationNotices = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBConnection.requireConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psPhieu = conn.prepareStatement(sqlPhieu)) {
                psPhieu.setDouble(1, tongTienPhat);
                psPhieu.setInt(2, maPhieuMuon);
                if (psPhieu.executeUpdate() == 0) {
                    conn.rollback();
                    return null;
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
                        return null;
                    }

                    if (!"Mất sách".equalsIgnoreCase(ct.getTinhTrangKhiTra())) {
                        psKho.setInt(1, ct.getSoLuong());
                        psKho.setInt(2, ct.getMaSach());
                        psKho.setInt(3, ct.getSoLuong());
                        if (psKho.executeUpdate() == 0) {
                            conn.rollback();
                            return null;
                        }
                        // Chuyển các phiếu đặt trước đang chờ (FIFO) sang 'Đã có sách' (giữ chỗ 3 ngày)
                        reservationNotices.addAll(
                                DatTruocDAO.promoteNextWaitingReservations(conn, ct.getMaSach(), ct.getSoLuong(), 3)
                        );
                    } else {
                        psMat.setInt(1, ct.getSoLuong());
                        psMat.setInt(2, ct.getSoLuong());
                        psMat.setInt(3, ct.getMaSach());
                        psMat.setInt(4, ct.getSoLuong());
                        if (psMat.executeUpdate() == 0) {
                            conn.rollback();
                            return null;
                        }
                        if (maNV > 0) {
                            SachDAO.insertLichSuKho(
                                    conn,
                                    ct.getMaSach(),
                                    maNV,
                                    "Mất sách",
                                    ct.getSoLuong(),
                                    "Mất sách khi trả phiếu #" + maPhieuMuon
                            );
                        }
                    }
                }
            }

            conn.commit();
            return reservationNotices;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return null;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
}