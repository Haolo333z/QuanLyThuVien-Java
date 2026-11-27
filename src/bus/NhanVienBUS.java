package bus;

import dao.NhanVienDAO;
import dto.NhanVienDTO;
import utils.PasswordUtil;
import utils.Session;
import utils.ValidationUtil;

import java.util.List;

public class NhanVienBUS {
    private static final int MIN_PASSWORD_LENGTH = 6;
    private NhanVienDAO nhanVienDAO = new NhanVienDAO();

    public NhanVienDTO dangNhap(String tenDangNhap, String matKhau) {
        String username = ValidationUtil.safeTrim(tenDangNhap);
        if (username.isEmpty() || matKhau == null || matKhau.isEmpty()) {
            return null;
        }
        NhanVienDTO nv = nhanVienDAO.findActiveByUsername(username);
        if (nv == null || !PasswordUtil.verifyPassword(matKhau, nv.getMatKhauHash())) {
            return null;
        }
        // Tự động nâng cấp mật khẩu cũ dạng plaintext lên PBKDF2 ngay khi đăng nhập thành công
        if (PasswordUtil.needsRehash(nv.getMatKhauHash())) {
            String hashed = PasswordUtil.hashPassword(matKhau);
            if (nhanVienDAO.updateMatKhauHash(nv.getMaNV(), hashed)) {
                nv.setMatKhauHash(hashed);
            }
        }
        return nv;
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

    private String validateCommon(NhanVienDTO nv) {
        if (nv == null) {
            return "Thông tin nhân viên không hợp lệ!";
        }
        String hoTen = ValidationUtil.safeTrim(nv.getHoTen());
        String vaiTro = ValidationUtil.safeTrim(nv.getVaiTro());
        String sdt = ValidationUtil.safeTrim(nv.getSoDienThoai());
        String email = ValidationUtil.safeTrim(nv.getEmail());

        if (hoTen.isEmpty()) {
            return "Họ tên nhân viên không được để trống!";
        }
        if (!"Admin".equalsIgnoreCase(vaiTro) && !"ThuThu".equalsIgnoreCase(vaiTro)) {
            return "Vai trò nhân viên không hợp lệ!";
        }
        if (!sdt.isEmpty() && !ValidationUtil.isValidPhone(sdt)) {
            return "Số điện thoại không hợp lệ (phải gồm 10-11 chữ số, bắt đầu bằng số 0)!";
        }
        if (!email.isEmpty() && !ValidationUtil.isValidEmail(email)) {
            return "Địa chỉ Email không đúng định dạng!";
        }

        nv.setHoTen(hoTen);
        nv.setVaiTro("Admin".equalsIgnoreCase(vaiTro) ? "Admin" : "ThuThu");
        nv.setSoDienThoai(sdt);
        nv.setEmail(email);
        return "OK";
    }

    public String addNhanVien(NhanVienDTO nv) {
        String check = validateCommon(nv);
        if (!"OK".equals(check)) {
            return check;
        }
        String username = ValidationUtil.safeTrim(nv.getTenDangNhap());
        String rawPass = nv.getMatKhauHash() != null ? nv.getMatKhauHash() : "";

        if (username.isEmpty() || username.contains(" ") || username.length() < 3) {
            return "Tên đăng nhập phải từ 3 ký tự trở lên và không chứa khoảng trắng!";
        }
        if (rawPass.trim().length() < MIN_PASSWORD_LENGTH) {
            return "Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự!";
        }
        if (nhanVienDAO.isUsernameExists(username)) {
            return "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!";
        }

        nv.setTenDangNhap(username);
        nv.setMatKhauHash(PasswordUtil.hashPassword(rawPass));
        return nhanVienDAO.insert(nv) ? "OK" : "Thêm nhân viên thất bại!";
    }

    public String updateNhanVien(NhanVienDTO nv) {
        if (nv == null || nv.getMaNV() <= 0) {
            return "Vui lòng chọn nhân viên cần cập nhật!";
        }
        String check = validateCommon(nv);
        if (!"OK".equals(check)) {
            return check;
        }
        if (Session.getCurrentMaNV() == nv.getMaNV() && !nv.isTrangThai()) {
            return "Không thể tự khóa tài khoản đang đăng nhập!";
        }

        NhanVienDTO existing = nhanVienDAO.getById(nv.getMaNV());
        if (existing == null) {
            return "Không tìm thấy nhân viên trong hệ thống!";
        }
        boolean wasActiveAdmin = "Admin".equalsIgnoreCase(existing.getVaiTro()) && existing.isTrangThai();
        boolean willStayActiveAdmin = "Admin".equalsIgnoreCase(nv.getVaiTro()) && nv.isTrangThai();
        if (wasActiveAdmin && !willStayActiveAdmin && nhanVienDAO.countActiveAdmins() <= 1) {
            return "Không thể khóa hoặc hạ quyền tài khoản Admin hoạt động cuối cùng!";
        }

        String newPass = nv.getMatKhauHash() != null ? nv.getMatKhauHash() : "";
        if (!newPass.isEmpty()) {
            if (newPass.trim().length() < MIN_PASSWORD_LENGTH) {
                return "Mật khẩu mới phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự!";
            }
            nv.setMatKhauHash(PasswordUtil.hashPassword(newPass));
            return nhanVienDAO.update(nv) ? "OK" : "Cập nhật nhân viên thất bại!";
        }
        return nhanVienDAO.updateWithoutPassword(nv) ? "OK" : "Cập nhật nhân viên thất bại!";
    }

    public String toggleTrangThai(int maNV, boolean trangThaiMoi) {
        if (maNV <= 0) {
            return "Vui lòng chọn nhân viên hợp lệ!";
        }
        if (Session.getCurrentMaNV() == maNV && !trangThaiMoi) {
            return "Không thể tự khóa tài khoản đang đăng nhập!";
        }
        if (!trangThaiMoi) {
            NhanVienDTO existing = nhanVienDAO.getById(maNV);
            if (existing != null && "Admin".equalsIgnoreCase(existing.getVaiTro()) && existing.isTrangThai()
                    && nhanVienDAO.countActiveAdmins() <= 1) {
                return "Không thể khóa tài khoản Admin hoạt động cuối cùng!";
            }
        }
        return nhanVienDAO.updateTrangThai(maNV, trangThaiMoi) ? "OK" : "Cập nhật trạng thái thất bại!";
    }

    public String doiMatKhau(int maNV, String matKhauCu, String matKhauMoi, String xacNhanMoi) {
        if (maNV <= 0) {
            return "Phiên đăng nhập không hợp lệ!";
        }
        if (matKhauCu == null || matKhauCu.isEmpty()
                || matKhauMoi == null || matKhauMoi.isEmpty()
                || xacNhanMoi == null || xacNhanMoi.isEmpty()) {
            return "Vui lòng nhập đầy đủ mật khẩu hiện tại, mật khẩu mới và xác nhận!";
        }
        if (matKhauMoi.trim().length() < MIN_PASSWORD_LENGTH) {
            return "Mật khẩu mới phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự!";
        }
        if (!matKhauMoi.equals(xacNhanMoi)) {
            return "Xác nhận mật khẩu mới không khớp!";
        }
        if (matKhauMoi.equals(matKhauCu)) {
            return "Mật khẩu mới phải khác mật khẩu hiện tại!";
        }

        NhanVienDTO nv = nhanVienDAO.getById(maNV);
        if (nv == null || !nv.isTrangThai()) {
            return "Tài khoản không tồn tại hoặc đã bị khóa!";
        }
        if (!PasswordUtil.verifyPassword(matKhauCu, nv.getMatKhauHash())) {
            return "Mật khẩu hiện tại không chính xác!";
        }

        String newHash = PasswordUtil.hashPassword(matKhauMoi);
        if (nhanVienDAO.updateMatKhauHash(maNV, newHash)) {
            if (Session.getCurrentMaNV() == maNV && Session.getCurrentUser() != null) {
                Session.getCurrentUser().setMatKhauHash(newHash);
            }
            return "OK";
        }
        return "Đổi mật khẩu thất bại!";
    }
}