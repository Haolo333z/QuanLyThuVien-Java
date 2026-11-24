package bus;

import dao.ThongKeDAO;

import java.util.List;

public class ThongKeBUS {
    private ThongKeDAO thongKeDAO = new ThongKeDAO();

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

    public List<Object[]> getTopSachMuonNhieu() {
        return thongKeDAO.getTopSachMuonNhieu();
    }

    public List<Object[]> getDanhSachQuaHan() {
        return thongKeDAO.getDanhSachQuaHan();
    }
}