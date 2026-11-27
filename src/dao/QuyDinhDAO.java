package dao;

import dto.QuyDinhDTO;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QuyDinhDAO {

    public QuyDinhDTO getQuyDinh() {
        String sql = "SELECT TOP 1 * FROM QuyDinh ORDER BY MaQuyDinh ASC";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                QuyDinhDTO qd = new QuyDinhDTO();
                qd.setMaQuyDinh(rs.getInt("MaQuyDinh"));
                qd.setSoNgayMuonMacDinh(rs.getInt("SoNgayMuonMacDinh"));
                qd.setSoNgayGiaHanMacDinh(rs.getInt("SoNgayGiaHanMacDinh"));
                qd.setSoLanGiaHanToiDa(rs.getInt("SoLanGiaHanToiDa"));
                qd.setSoSachToiDa(rs.getInt("SoSachToiDa"));
                qd.setPhatTreMoiNgay(rs.getLong("PhatTreMoiNgay"));
                qd.setPhatHuHong(rs.getLong("PhatHuHong"));
                qd.setPhatMatSach(rs.getLong("PhatMatSach"));
                return qd;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new QuyDinhDTO();
    }

    public boolean updateQuyDinh(QuyDinhDTO qd) {
        String sqlUpdate = "UPDATE QuyDinh SET SoNgayMuonMacDinh = ?, SoNgayGiaHanMacDinh = ?, SoLanGiaHanToiDa = ?, "
                         + "SoSachToiDa = ?, PhatTreMoiNgay = ?, PhatHuHong = ?, PhatMatSach = ? WHERE MaQuyDinh = ?";
        String sqlCount = "SELECT COUNT(*) FROM QuyDinh";
        String sqlInsert = "INSERT INTO QuyDinh (SoNgayMuonMacDinh, SoNgayGiaHanMacDinh, SoLanGiaHanToiDa, "
                         + "SoSachToiDa, PhatTreMoiNgay, PhatHuHong, PhatMatSach) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.requireConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setInt(1, qd.getSoNgayMuonMacDinh());
                ps.setInt(2, qd.getSoNgayGiaHanMacDinh());
                ps.setInt(3, qd.getSoLanGiaHanToiDa());
                ps.setInt(4, qd.getSoSachToiDa());
                ps.setLong(5, qd.getPhatTreMoiNgay());
                ps.setLong(6, qd.getPhatHuHong());
                ps.setLong(7, qd.getPhatMatSach());
                ps.setInt(8, qd.getMaQuyDinh());
                if (ps.executeUpdate() > 0) {
                    return true;
                }
            }
            try (PreparedStatement psCount = conn.prepareStatement(sqlCount);
                 ResultSet rs = psCount.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                        psIns.setInt(1, qd.getSoNgayMuonMacDinh());
                        psIns.setInt(2, qd.getSoNgayGiaHanMacDinh());
                        psIns.setInt(3, qd.getSoLanGiaHanToiDa());
                        psIns.setInt(4, qd.getSoSachToiDa());
                        psIns.setLong(5, qd.getPhatTreMoiNgay());
                        psIns.setLong(6, qd.getPhatHuHong());
                        psIns.setLong(7, qd.getPhatMatSach());
                        return psIns.executeUpdate() > 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}