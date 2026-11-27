package bus;

import dao.DocGiaDAO;
import dto.DocGiaDTO;
import utils.ValidationUtil;

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

    private String validateAndPrepare(DocGiaDTO dg, boolean isUpdate) {
        if (dg == null) {
            return "Thông tin độc giả không hợp lệ!";
        }
        String hoTen = ValidationUtil.safeTrim(dg.getHoTen());
        String sdt = ValidationUtil.safeTrim(dg.getSoDienThoai());
        String email = ValidationUtil.safeTrim(dg.getEmail());
        String diaChi = ValidationUtil.safeTrim(dg.getDiaChi());

        if (hoTen.isEmpty() || sdt.isEmpty()) {
            return "Vui lòng nhập đầy đủ Họ tên và Số điện thoại!";
        }
        if (!ValidationUtil.isValidPhone(sdt)) {
            return "Số điện thoại không hợp lệ (phải gồm 10-11 chữ số, bắt đầu bằng số 0)!";
        }
        if (!email.isEmpty() && !ValidationUtil.isValidEmail(email)) {
            return "Địa chỉ Email không đúng định dạng!";
        }
        if (dg.getNgaySinh() != null && dg.getNgaySinh().isAfter(LocalDate.now())) {
            return "Ngày sinh không được lớn hơn ngày hiện tại!";
        }

        dg.setHoTen(hoTen);
        dg.setSoDienThoai(sdt);
        dg.setEmail(email);
        dg.setDiaChi(diaChi);

        int excludeId = isUpdate ? dg.getMaDocGia() : 0;
        DocGiaDTO existing = docGiaDAO.findByPhoneOrEmail(sdt, email, excludeId);
        if (existing != null) {
            if (isUpdate) {
                return "Số điện thoại hoặc email này đã thuộc về độc giả khác (Mã ĐG: "
                        + existing.getMaDocGia() + " - " + existing.getHoTen() + ")!";
            }
            if (existing.isTrangThai()) {
                return "Số điện thoại hoặc email này đã có thẻ đang hoạt động (Mã ĐG: "
                        + existing.getMaDocGia() + " - " + existing.getHoTen()
                        + "). Vui lòng dùng chức năng Gia hạn hoặc Cập nhật!";
            }
            return "Số điện thoại hoặc email này đã có thẻ đang bị KHÓA (Mã ĐG: "
                    + existing.getMaDocGia() + " - " + existing.getHoTen()
                    + "). Vui lòng mở khóa thẻ cũ thay vì lập thẻ mới!";
        }
        return "OK";
    }

    public String addDocGia(DocGiaDTO dg) {
        String check = validateAndPrepare(dg, false);
        if (!"OK".equals(check)) {
            return check;
        }

        LocalDate today = LocalDate.now();
        dg.setNgayLapThe(today);
        dg.setNgayHetHan(today.plusYears(1)); // Tự động +1 năm
        return docGiaDAO.insert(dg) ? "OK" : "Thêm độc giả thất bại!";
    }

    public String updateDocGia(DocGiaDTO dg) {
        if (dg == null || dg.getMaDocGia() <= 0) {
            return "Vui lòng chọn độc giả cần cập nhật!";
        }
        String check = validateAndPrepare(dg, true);
        if (!"OK".equals(check)) {
            return check;
        }
        return docGiaDAO.update(dg) ? "OK" : "Cập nhật độc giả thất bại!";
    }

    public String giaHanThe(int maDocGia, LocalDate ngayHetHanHienTai) {
        if (maDocGia <= 0) {
            return "Vui lòng chọn độc giả cần gia hạn!";
        }
        LocalDate today = LocalDate.now();
        // Nếu thẻ đã hết hạn thì tính từ hôm nay + 1 năm, còn hạn thì cộng tiếp 1 năm
        LocalDate baseDate = (ngayHetHanHienTai == null || ngayHetHanHienTai.isBefore(today))
                ? today
                : ngayHetHanHienTai;
        LocalDate ngayMoi = baseDate.plusYears(1);
        return docGiaDAO.giaHanThe(maDocGia, ngayMoi) ? "OK:" + ngayMoi : "Gia hạn thẻ thất bại!";
    }

    public boolean toggleTrangThai(int maDocGia, boolean trangThaiMoi) {
        return docGiaDAO.updateTrangThai(maDocGia, trangThaiMoi);
    }
}