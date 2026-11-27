package dao;

import dto.DatTruocDTO;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatTruocDAO {

    public void autoExpireHolds() {
        String sql = "UPDATE DatTruocSach "
                   + "SET TrangThai = N'Đã hủy', "
                   + "    GhiChu = CASE WHEN GhiChu IS NULL OR GhiChu = N'' THEN N'Quá hạn giữ chỗ' "
                   + "                  ELSE GhiChu + N' | Quá hạn giữ chỗ' END "
                   + "WHERE TrangThai = N'Đã có sách' AND HanGiuCho IS NOT NULL AND HanGiuCho < CAST(GETDATE() AS DATE)";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<DatTruocDTO> search(String keyword, String trangThaiFilter) {
        List<DatTruocDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT dt.*, dg.HoTen AS TenDocGia, dg.SoDienThoai AS SdtDocGia, "
              + "       s.TenSach, nv.HoTen AS TenNV "
              + "FROM DatTruocSach dt "
              + "JOIN DocGia dg ON dt.MaDocGia = dg.MaDocGia "
              + "JOIN Sach s ON dt.MaSach = s.MaSach "
              + "JOIN NhanVien nv ON dt.MaNV = nv.MaNV "
              + "WHERE 1=1 ");

        String cleanKey = keyword != null ? keyword.trim() : "";
        if (!cleanKey.isEmpty()) {
            sql.append("AND (dg.HoTen LIKE ? OR dg.SoDienThoai LIKE ? OR s.TenSach LIKE ? OR CAST(dt.MaDatTruoc AS VARCHAR(20)) LIKE ?) ");
        }
        String cleanFilter = trangThaiFilter != null ? trangThaiFilter.trim() : "Tất cả";
        if (!"Tất cả".equalsIgnoreCase(cleanFilter)) {
            sql.append("AND dt.TrangThai = ? ");
        }
        sql.append("ORDER BY CASE WHEN dt.TrangThai = N'Đã có sách' THEN 0 WHEN dt.TrangThai = N'Đang chờ' THEN 1 ELSE 2 END, dt.MaDatTruoc DESC");

        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (!cleanKey.isEmpty()) {
                String pattern = "%" + cleanKey + "%";
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
            }
            if (!"Tất cả".equalsIgnoreCase(cleanFilter)) {
                ps.setString(idx, cleanFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("NgayDat");
                    Date han = rs.getDate("HanGiuCho");
                    list.add(new DatTruocDTO(
                            rs.getInt("MaDatTruoc"),
                            rs.getInt("MaDocGia"),
                            rs.getString("TenDocGia"),
                            rs.getString("SdtDocGia"),
                            rs.getInt("MaSach"),
                            rs.getString("TenSach"),
                            rs.getInt("MaNV"),
                            rs.getString("TenNV"),
                            ts != null ? ts.toLocalDateTime() : null,
                            han != null ? han.toLocalDate() : null,
                            rs.getString("TrangThai"),
                            rs.getString("GhiChu")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean hasActiveReservation(int maDocGia, int maSach) {
        String sql = "SELECT 1 FROM DatTruocSach WHERE MaDocGia = ? AND MaSach = ? AND TrangThai IN (N'Đang chờ', N'Đã có sách')";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDocGia);
            ps.setInt(2, maSach);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isReaderBorrowingBook(int maDocGia, int maSach) {
        String sql = "SELECT 1 FROM PhieuMuon pm "
                   + "JOIN ChiTietPhieuMuon ct ON pm.MaPhieuMuon = ct.MaPhieuMuon "
                   + "WHERE pm.MaDocGia = ? AND ct.MaSach = ? AND pm.TrangThai <> N'Đã trả'";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDocGia);
            ps.setInt(2, maSach);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countHeldCopiesForOtherReaders(int maSach, int excludeMaDocGia) {
        String sql = "SELECT COUNT(*) FROM DatTruocSach "
                   + "WHERE MaSach = ? AND MaDocGia <> ? AND TrangThai = N'Đã có sách' "
                   + "AND (HanGiuCho IS NULL OR HanGiuCho >= CAST(GETDATE() AS DATE))";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maSach);
            ps.setInt(2, excludeMaDocGia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean insert(DatTruocDTO dt) {
        String sql = "INSERT INTO DatTruocSach (MaDocGia, MaSach, MaNV, TrangThai, GhiChu) VALUES (?, ?, ?, N'Đang chờ', ?)";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dt.getMaDocGia());
            ps.setInt(2, dt.getMaSach());
            ps.setInt(3, dt.getMaNV());
            ps.setString(4, dt.getGhiChu() != null && !dt.getGhiChu().trim().isEmpty() ? dt.getGhiChu().trim() : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelReservation(int maDatTruoc) {
        String sql = "UPDATE DatTruocSach SET TrangThai = N'Đã hủy' WHERE MaDatTruoc = ? AND TrangThai IN (N'Đang chờ', N'Đã có sách')";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDatTruoc);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Chuyển tối đa returnedQty phiếu đặt trước đang chờ (FIFO) sang 'Đã có sách' khi sách được trả vào kho.
     */
    public static List<String> promoteNextWaitingReservations(Connection conn, int maSach, int returnedQty, int holdDays)
            throws SQLException {
        List<String> promotedMessages = new ArrayList<>();
        if (returnedQty <= 0) {
            return promotedMessages;
        }

        String sqlSelect = "SELECT TOP (?) dt.MaDatTruoc, dg.HoTen, dg.SoDienThoai, s.TenSach "
                         + "FROM DatTruocSach dt "
                         + "JOIN DocGia dg ON dt.MaDocGia = dg.MaDocGia "
                         + "JOIN Sach s ON dt.MaSach = s.MaSach "
                         + "WHERE dt.MaSach = ? AND dt.TrangThai = N'Đang chờ' "
                         + "ORDER BY dt.NgayDat ASC, dt.MaDatTruoc ASC";
        String sqlUpdate = "UPDATE DatTruocSach "
                         + "SET TrangThai = N'Đã có sách', HanGiuCho = DATEADD(DAY, ?, CAST(GETDATE() AS DATE)) "
                         + "WHERE MaDatTruoc = ? AND TrangThai = N'Đang chờ'";

        List<Integer> idsToPromote = new ArrayList<>();
        try (PreparedStatement psSel = conn.prepareStatement(sqlSelect)) {
            psSel.setInt(1, returnedQty);
            psSel.setInt(2, maSach);
            try (ResultSet rs = psSel.executeQuery()) {
                while (rs.next()) {
                    idsToPromote.add(rs.getInt("MaDatTruoc"));
                    promotedMessages.add(String.format(
                            "%s (SĐT: %s) - Sách: %s",
                            rs.getString("HoTen"),
                            rs.getString("SoDienThoai"),
                            rs.getString("TenSach")
                    ));
                }
            }
        }

        if (!idsToPromote.isEmpty()) {
            try (PreparedStatement psUpd = conn.prepareStatement(sqlUpdate)) {
                for (int id : idsToPromote) {
                    psUpd.setInt(1, holdDays);
                    psUpd.setInt(2, id);
                    psUpd.executeUpdate();
                }
            }
        }
        return promotedMessages;
    }

    public static void markReaderReservationsReceived(Connection conn, int maDocGia, int maSach) throws SQLException {
        String sql = "UPDATE DatTruocSach SET TrangThai = N'Đã nhận' "
                   + "WHERE MaDocGia = ? AND MaSach = ? AND TrangThai IN (N'Đang chờ', N'Đã có sách')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDocGia);
            ps.setInt(2, maSach);
            ps.executeUpdate();
        }
    }
}
