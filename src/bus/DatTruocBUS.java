package bus;

import dao.DatTruocDAO;
import dao.DocGiaDAO;
import dao.SachDAO;
import dto.DatTruocDTO;
import dto.DocGiaDTO;
import dto.SachDTO;

import java.time.LocalDate;
import java.util.List;

public class DatTruocBUS {
    private DatTruocDAO datTruocDAO = new DatTruocDAO();
    private DocGiaDAO docGiaDAO = new DocGiaDAO();
    private SachDAO sachDAO = new SachDAO();

    public List<DatTruocDTO> search(String keyword, String trangThaiFilter) {
        datTruocDAO.autoExpireHolds();
        return datTruocDAO.search(keyword, trangThaiFilter);
    }

    public String datTruocSach(int maDocGia, int maSach, int maNV, String ghiChu) {
        if (maNV <= 0) {
            return "Không xác định được nhân viên thực hiện! Vui lòng đăng nhập lại.";
        }
        if (maDocGia <= 0 || maSach <= 0) {
            return "Vui lòng chọn đầy đủ Độc giả và Đầu sách!";
        }

        DocGiaDTO dg = docGiaDAO.getById(maDocGia);
        if (dg == null) {
            return "Không tìm thấy thông tin độc giả!";
        }
        if (!dg.isTrangThai()) {
            return "Thẻ độc giả đang bị KHÓA, không thể đặt trước sách!";
        }
        if (dg.getNgayHetHan() != null && dg.getNgayHetHan().isBefore(LocalDate.now())) {
            return "Thẻ độc giả đã HẾT HẠN (" + dg.getNgayHetHan() + "), vui lòng gia hạn thẻ trước!";
        }

        SachDTO sach = sachDAO.getById(maSach);
        if (sach == null) {
            return "Không tìm thấy thông tin đầu sách!";
        }
        if (!sach.isTrangThai()) {
            return "Đầu sách này đã ngừng lưu hành!";
        }
        if (sach.getSoLuongConLai() > 0) {
            return "Đầu sách này hiện vẫn còn " + sach.getSoLuongConLai() + " cuốn trong kho, vui lòng lập phiếu mượn trực tiếp!";
        }

        if (datTruocDAO.isReaderBorrowingBook(maDocGia, maSach)) {
            return "Độc giả đang mượn cuốn sách này chưa trả, không thể đặt trước thêm!";
        }
        if (datTruocDAO.hasActiveReservation(maDocGia, maSach)) {
            return "Độc giả đã có yêu cầu đặt trước đang chờ / giữ chỗ cho đầu sách này!";
        }

        DatTruocDTO dt = new DatTruocDTO();
        dt.setMaDocGia(maDocGia);
        dt.setMaSach(maSach);
        dt.setMaNV(maNV);
        dt.setGhiChu(ghiChu);
        return datTruocDAO.insert(dt) ? "OK" : "Đăng ký đặt trước thất bại (kiểm tra bảng DatTruocSach trong CSDL)!";
    }

    public String huyDatTruoc(int maDatTruoc) {
        if (maDatTruoc <= 0) {
            return "Vui lòng chọn phiếu đặt trước cần hủy!";
        }
        return datTruocDAO.cancelReservation(maDatTruoc)
                ? "OK"
                : "Chỉ có thể hủy các đặt trước ở trạng thái 'Đang chờ' hoặc 'Đã có sách'!";
    }
}
