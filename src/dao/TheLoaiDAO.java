package dao;

import dto.TheLoaiDTO;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TheLoaiDAO {

    public List<TheLoaiDTO> getAll() {
        List<TheLoaiDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM TheLoai ORDER BY MaTheLoai ASC";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new TheLoaiDTO(
                        rs.getInt("MaTheLoai"),
                        rs.getString("TenTheLoai"),
                        rs.getString("MoTa")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(TheLoaiDTO tl) {
        String sql = "INSERT INTO TheLoai (TenTheLoai, MoTa) VALUES (?, ?)";
        try (Connection conn = DBConnection.requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tl.getTenTheLoai());
            ps.setString(2, tl.getMoTa());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}