package bus;

import dao.NhanVienDAO;
import dto.NhanVienDTO;

public class NhanVienBUS {
    private NhanVienDAO nhanVienDAO = new NhanVienDAO();

    public NhanVienDTO dangNhap(String tenDangNhap, String matKhau) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty() ||
            matKhau == null || matKhau.trim().isEmpty()) {
            return null;
        }
        return nhanVienDAO.checkLogin(tenDangNhap.trim(), matKhau.trim());
    }
}