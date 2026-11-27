package bus;

import dao.SachDAO;
import dao.TheLoaiDAO;
import dto.LichSuKhoDTO;
import dto.SachDTO;
import dto.TheLoaiDTO;
import utils.Session;

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
        if (s.getTenSach() == null || s.getTenSach().trim().isEmpty()
                || s.getTacGia() == null || s.getTacGia().trim().isEmpty()) {
            return "Vui lòng nhập đầy đủ Tên sách và Tác giả!";
        }
        int currentYear = Year.now().getValue();
        if (s.getNamXuatBan() < 1900 || s.getNamXuatBan() > currentYear) {
            return "Năm xuất bản phải từ 1900 đến " + currentYear + "!";
        }
        if (s.getSoLuongTong() < 0 || s.getSoLuongConLai() < 0 || s.getSoLuongThanhLy() < 0) {
            return "Số lượng tổng, số lượng còn lại và số lượng thanh lý không được âm!";
        }
        if (s.getSoLuongConLai() > s.getSoLuongTong()) {
            return "Số lượng còn lại không được lớn hơn số lượng tổng!";
        }
        if (s.getSoLuongConLai() + s.getSoLuongThanhLy() > s.getSoLuongTong()) {
            return "Tổng số lượng còn lại và đã thanh lý không được lớn hơn số lượng tổng!";
        }
        if (sachDAO.isIsbnExists(s.getIsbn(), s.getMaSach())) {
            return "Mã ISBN này đã tồn tại trong hệ thống!";
        }
        return "OK";
    }

    public String addSach(SachDTO s) {
        if (s.getTinhTrangSach() == null || s.getTinhTrangSach().trim().isEmpty()) {
            s.setTinhTrangSach("Bình thường");
        }
        String check = validateSach(s);
        if (!"OK".equals(check)) return check;
        return sachDAO.insert(s, Session.getCurrentMaNV()) ? "OK" : "Thêm sách thất bại!";
    }

    public String updateSach(SachDTO s) {
        if (s.getTinhTrangSach() == null || s.getTinhTrangSach().trim().isEmpty()) {
            s.setTinhTrangSach("Bình thường");
        }
        String check = validateSach(s);
        if (!"OK".equals(check)) return check;
        return sachDAO.update(s) ? "OK" : "Cập nhật sách thất bại!";
    }

    public boolean toggleTrangThai(int maSach, boolean trangThaiMoi) {
        return sachDAO.updateTrangThai(maSach, trangThaiMoi);
    }

    public String nhapThemSach(int maSach, int soLuongThem, String ghiChu) {
        if (Session.getCurrentMaNV() <= 0) {
            return "Không xác định được nhân viên thực hiện! Vui lòng đăng nhập lại.";
        }
        if (maSach <= 0) {
            return "Vui lòng chọn đầu sách cần nhập thêm!";
        }
        if (soLuongThem <= 0) {
            return "Số lượng nhập thêm phải lớn hơn 0!";
        }
        SachDTO target = sachDAO.getById(maSach);
        if (target == null) {
            return "Không tìm thấy thông tin đầu sách!";
        }
        String cleanNote = (ghiChu != null && !ghiChu.trim().isEmpty()) ? ghiChu.trim() : "Nhập bổ sung kho";
        boolean ok = sachDAO.nhapThemSachTransaction(maSach, soLuongThem, Session.getCurrentMaNV(), cleanNote);
        return ok ? "OK" : "Nhập thêm kho thất bại (kiểm tra bảng LichSuKhoSach trong CSDL)!";
    }

    public String thanhLySach(int maSach, int soLuongThanhLy, String tinhTrangMoi) {
        return thanhLySach(maSach, soLuongThanhLy, tinhTrangMoi, "Thanh lý sách từ kho");
    }

    public String thanhLySach(int maSach, int soLuongThanhLy, String tinhTrangMoi, String ghiChu) {
        if (maSach <= 0) {
            return "Vui lòng chọn đầu sách cần thanh lý!";
        }
        if (soLuongThanhLy <= 0) {
            return "Số lượng thanh lý phải lớn hơn 0!";
        }
        SachDTO target = sachDAO.getById(maSach);
        if (target == null) {
            return "Không tìm thấy thông tin đầu sách!";
        }
        if (soLuongThanhLy > target.getSoLuongConLai()) {
            return "Số lượng thanh lý (" + soLuongThanhLy + ") vượt quá số lượng sách đang còn trong kho (" + target.getSoLuongConLai() + ")!";
        }

        String tt = (tinhTrangMoi != null && !tinhTrangMoi.trim().isEmpty())
                ? tinhTrangMoi.trim()
                : "Thanh lý";
        String cleanNote = (ghiChu != null && !ghiChu.trim().isEmpty()) ? ghiChu.trim() : "Thanh lý sách từ kho";
        boolean ok = sachDAO.thanhLySach(maSach, soLuongThanhLy, tt, Session.getCurrentMaNV(), cleanNote);
        return ok ? "OK" : "Thanh lý sách thất bại (kiểm tra lại số lượng còn lại hoặc bảng LichSuKhoSach)!";
    }

    public List<LichSuKhoDTO> getLichSuKho(int maSachFilter, String loaiFilter) {
        return sachDAO.getLichSuKho(maSachFilter, loaiFilter);
    }
}