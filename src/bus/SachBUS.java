package bus;

import dao.SachDAO;
import dao.TheLoaiDAO;
import dto.SachDTO;
import dto.TheLoaiDTO;

import java.time.Year;
import java.util.List;

public class SachBUS {
    private SachDAO sachDAO = new SachDAO();
    private TheLoaiDAO theLoaiDAO = new TheLoaiDAO();

    public List<TheLoaiDTO> getAllTheLoai() {
        return theLoaiDAO.getAll();
    }

    public String addTheLoai(String tenTheLoai, String moTa) {
        if (tenTheLoai == null || tenTheLoai.trim().isEmpty()) {
            return "Tên thể loại không được để trống!";
        }
        return theLoaiDAO.insert(new TheLoaiDTO(0, tenTheLoai.trim(), moTa != null ? moTa.trim() : ""))
                ? "OK" : "Thêm thể loại thất bại (có thể tên thể loại đã tồn tại)!";
    }

    public List<SachDTO> getAllSach() {
        return sachDAO.getAll();
    }

    public List<SachDTO> searchSach(String keyword, int maTheLoai) {
        return sachDAO.search(keyword, maTheLoai);
    }

    private String validateSach(SachDTO s) {
        if (s.getTenSach().isEmpty() || s.getTacGia().isEmpty()) {
            return "Vui lòng nhập đầy đủ Tên sách và Tác giả!";
        }
        int currentYear = Year.now().getValue();
        if (s.getNamXuatBan() < 1900 || s.getNamXuatBan() > currentYear) {
            return "Năm xuất bản phải từ 1900 đến " + currentYear + "!";
        }
        if (s.getSoLuongTong() < 0 || s.getSoLuongConLai() < 0) {
            return "Số lượng tổng và số lượng còn lại không được âm!";
        }
        if (s.getSoLuongConLai() > s.getSoLuongTong()) {
            return "Số lượng còn lại không được lớn hơn số lượng tổng!";
        }
        if (sachDAO.isIsbnExists(s.getIsbn(), s.getMaSach())) {
            return "Mã ISBN này đã tồn tại trong hệ thống!";
        }
        return "OK";
    }

    public String addSach(SachDTO s) {
        String check = validateSach(s);
        if (!"OK".equals(check)) return check;
        return sachDAO.insert(s) ? "OK" : "Thêm sách thất bại!";
    }

    public String updateSach(SachDTO s) {
        String check = validateSach(s);
        if (!"OK".equals(check)) return check;
        return sachDAO.update(s) ? "OK" : "Cập nhật sách thất bại!";
    }

    public boolean toggleTrangThai(int maSach, boolean trangThaiMoi) {
        return sachDAO.updateTrangThai(maSach, trangThaiMoi);
    }
}