package bus;

import dao.QuyDinhDAO;
import dao.ThongKeDAO;
import dto.QuyDinhDTO;

import java.time.LocalDate;
import java.util.List;

public class ThongKeBUS {
    private ThongKeDAO thongKeDAO = new ThongKeDAO();
    private QuyDinhDAO quyDinhDAO = new QuyDinhDAO();

    public int getTongSoDauSach() {
        return thongKeDAO.getTongSoDauSach();
    }

    public int getTongSoDocGia() {
        return thongKeDAO.getTongSoDocGia();
    }

    public int getSoPhieuDangMuon() {
        return thongKeDAO.getSoPhieuDangMuon();
    }

    public double getTongTienPhat() {
        return thongKeDAO.getTongTienPhat();
    }

    public double getTienPhatDuKienQuaHan() {
        QuyDinhDTO qd = quyDinhDAO.getQuyDinh();
        long phatMoiNgay = (qd != null && qd.getPhatTreMoiNgay() >= 0) ? qd.getPhatTreMoiNgay() : 5000L;
        return thongKeDAO.getTienPhatDuKienQuaHan(phatMoiNgay);
    }

    public List<Object[]> getTopSachMuonNhieu() {
        return thongKeDAO.getTopSachMuonNhieu();
    }

    public List<Object[]> getDanhSachQuaHan() {
        return thongKeDAO.getDanhSachQuaHan();
    }

    public int getSoPhieuMuonTheoKy(LocalDate tuNgay, LocalDate denNgay) {
        return thongKeDAO.getSoPhieuMuonTheoKy(tuNgay, denNgay);
    }

    public int getSoPhieuDaTraTheoKy(LocalDate tuNgay, LocalDate denNgay) {
        return thongKeDAO.getSoPhieuDaTraTheoKy(tuNgay, denNgay);
    }

    public double getTongTienPhatTheoKy(LocalDate tuNgay, LocalDate denNgay) {
        return thongKeDAO.getTongTienPhatTheoKy(tuNgay, denNgay);
    }

    public List<Object[]> getThongKeTheoThang(LocalDate tuNgay, LocalDate denNgay) {
        return thongKeDAO.getThongKeTheoThang(tuNgay, denNgay);
    }

    public List<Object[]> getThongKeTheoTheLoai() {
        return thongKeDAO.getThongKeTheoTheLoai();
    }
}
