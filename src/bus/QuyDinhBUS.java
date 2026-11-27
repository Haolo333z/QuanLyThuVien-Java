package bus;

import dao.QuyDinhDAO;
import dto.QuyDinhDTO;
import utils.Session;

public class QuyDinhBUS {
    private QuyDinhDAO quyDinhDAO = new QuyDinhDAO();

    public QuyDinhDTO getQuyDinh() {
        QuyDinhDTO qd = quyDinhDAO.getQuyDinh();
        return qd != null ? qd : new QuyDinhDTO();
    }

    public String updateQuyDinh(QuyDinhDTO qd) {
        if (!Session.isAdmin()) {
            return "Chỉ Quản trị viên (Admin) mới có quyền thay đổi quy định thư viện!";
        }
        if (qd == null) {
            return "Dữ liệu quy định không hợp lệ!";
        }
        if (qd.getSoNgayMuonMacDinh() <= 0 || qd.getSoNgayMuonMacDinh() > 365) {
            return "Số ngày mượn mặc định phải từ 1 đến 365 ngày!";
        }
        if (qd.getSoNgayGiaHanMacDinh() <= 0 || qd.getSoNgayGiaHanMacDinh() > 90) {
            return "Số ngày gia hạn mặc định phải từ 1 đến 90 ngày!";
        }
        if (qd.getSoLanGiaHanToiDa() < 0 || qd.getSoLanGiaHanToiDa() > 10) {
            return "Số lần gia hạn tối đa phải từ 0 đến 10 lần!";
        }
        if (qd.getSoSachToiDa() <= 0 || qd.getSoSachToiDa() > 50) {
            return "Số sách mượn tối đa phải từ 1 đến 50 cuốn!";
        }
        if (qd.getPhatTreMoiNgay() < 0 || qd.getPhatHuHong() < 0 || qd.getPhatMatSach() < 0) {
            return "Các mức tiền phạt không được âm!";
        }
        return quyDinhDAO.updateQuyDinh(qd) ? "OK" : "Lưu quy định thất bại (kiểm tra bảng QuyDinh trong CSDL)!";
    }
}
