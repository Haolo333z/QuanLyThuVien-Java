package bus;

import dao.DocGiaDAO;
import dto.DocGiaDTO;

import java.time.LocalDate;
import java.util.List;

public class DocGiaBUS {
    private DocGiaDAO docGiaDAO = new DocGiaDAO();

    public List<DocGiaDTO> getAll() {
        return docGiaDAO.getAll();
    }

    public List<DocGiaDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return docGiaDAO.getAll();
        }
        return docGiaDAO.search(keyword.trim());
    }

    public String addDocGia(DocGiaDTO dg) {
        if (dg.getHoTen().isEmpty() || dg.getSoDienThoai().isEmpty()) {
            return "Vui lòng nhập đầy đủ Họ tên và Số điện thoại!";
        }
        LocalDate today = LocalDate.now();
        dg.setNgayLapThe(today);
        dg.setNgayHetHan(today.plusYears(1)); // Tự động +1 năm
        return docGiaDAO.insert(dg) ? "OK" : "Thêm độc giả thất bại!";
    }

    public String updateDocGia(DocGiaDTO dg) {
        if (dg.getHoTen().isEmpty() || dg.getSoDienThoai().isEmpty()) {
            return "Họ tên và Số điện thoại không được để trống!";
        }
        return docGiaDAO.update(dg) ? "OK" : "Cập nhật độc giả thất bại!";
    }

    public String giaHanThe(int maDocGia, LocalDate ngayHetHanHienTai) {
        LocalDate today = LocalDate.now();
        // Nếu thẻ đã hết hạn thì tính từ hôm nay + 1 năm, còn hạn thì cộng tiếp 1 năm
        LocalDate baseDate = ngayHetHanHienTai.isBefore(today) ? today : ngayHetHanHienTai;
        LocalDate ngayMoi = baseDate.plusYears(1);
        return docGiaDAO.giaHanThe(maDocGia, ngayMoi) ? "OK:" + ngayMoi : "Gia hạn thẻ thất bại!";
    }

    public boolean toggleTrangThai(int maDocGia, boolean trangThaiMoi) {
        return docGiaDAO.updateTrangThai(maDocGia, trangThaiMoi);
    }
}