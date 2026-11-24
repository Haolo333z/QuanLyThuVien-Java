package bus;

import dao.NhanVienDAO;
import dto.NhanVienDTO;

import java.util.List;

public class NhanVienBUS {
    private NhanVienDAO nhanVienDAO = new NhanVienDAO();

    public NhanVienDTO dangNhap(String tenDangNhap, String matKhau) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty() ||
            matKhau == null || matKhau.trim().isEmpty()) {
            return null;
        }
        return nhanVienDAO.checkLogin(tenDangNhap.trim(), matKhau.trim());
    }

    public List<NhanVienDTO> getAll() {
        return nhanVienDAO.getAll();
    }

    public List<NhanVienDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return nhanVienDAO.getAll();
        }
        return nhanVienDAO.search(keyword.trim());
    }

    public String addNhanVien(NhanVienDTO nv) {
        if (nv.getHoTen().isEmpty() || nv.getTenDangNhap().isEmpty() || nv.getMatKhauHash().isEmpty()) {
            return "Vui lòng nhập đầy đủ Họ tên, Tên đăng nhập và Mật khẩu!";
        }
        if (nhanVienDAO.isUsernameExists(nv.getTenDangNhap())) {
            return "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!";
        }
        return nhanVienDAO.insert(nv) ? "OK" : "Thêm nhân viên thất bại!";
    }

    public String updateNhanVien(NhanVienDTO nv) {
        if (nv.getHoTen().isEmpty() || nv.getMatKhauHash().isEmpty()) {
            return "Họ tên và Mật khẩu không được để trống!";
        }
        return nhanVienDAO.update(nv) ? "OK" : "Cập nhật nhân viên thất bại!";
    }

    public boolean toggleTrangThai(int maNV, boolean trangThaiMoi) {
        return nhanVienDAO.updateTrangThai(maNV, trangThaiMoi);
    }
}