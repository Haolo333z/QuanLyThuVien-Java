package bus;

import dao.DocGiaDAO;
import dao.PhieuMuonDAO;
import dto.ChiTietPhieuMuonDTO;
import dto.DocGiaDTO;
import dto.PhieuMuonDTO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MuonTraBUS {
    public static final int MAX_SACH_MUON = 5;
    public static final double PHAT_TRE_MOI_NGAY = 5000;
    public static final double PHAT_HU_HONG = 50000;
    public static final double PHAT_MAT_SACH = 150000;

    private PhieuMuonDAO phieuMuonDAO = new PhieuMuonDAO();
    private DocGiaDAO docGiaDAO = new DocGiaDAO();

    public List<PhieuMuonDTO> getPhieuDangMuon() {
        return phieuMuonDAO.getAll(true);
    }

    public List<ChiTietPhieuMuonDTO> getChiTiet(int maPhieuMuon) {
        return phieuMuonDAO.getChiTietByPhieu(maPhieuMuon);
    }

    public String lapPhieuMuon(int maDocGia, int maNV, LocalDate hanTra, List<ChiTietPhieuMuonDTO> dsSach) {
        if (dsSach == null || dsSach.isEmpty()) {
            return "Vui lòng chọn ít nhất 1 cuốn sách để mượn!";
        }
        if (hanTra == null || hanTra.isBefore(LocalDate.now())) {
            return "Hạn trả không được nhỏ hơn ngày hiện tại!";
        }

        // 1. Kiểm tra thẻ độc giả
        DocGiaDTO dg = docGiaDAO.getById(maDocGia);
        if (dg == null) return "Không tìm thấy thông tin độc giả!";
        if (!dg.isTrangThai()) return "Thẻ độc giả đang bị KHÓA, không thể mượn sách!";
        if (dg.getNgayHetHan().isBefore(LocalDate.now())) {
            return "Thẻ độc giả đã HẾT HẠN (" + dg.getNgayHetHan() + "), vui lòng gia hạn trước khi mượn!";
        }

        // 2. Kiểm tra có phiếu quá hạn chưa trả không
        if (phieuMuonDAO.hasPhieuQuaHan(maDocGia)) {
            return "Độc giả đang có phiếu mượn QUÁ HẠN chưa trả, không được mượn thêm!";
        }

        // 3. Kiểm tra giới hạn tối đa 5 cuốn
        int dangMuon = phieuMuonDAO.countSachDangMuon(maDocGia);
        int muonMoi = dsSach.stream().mapToInt(ChiTietPhieuMuonDTO::getSoLuong).sum();
        if (dangMuon + muonMoi > MAX_SACH_MUON) {
            return "Vượt quá giới hạn tối đa " + MAX_SACH_MUON + " cuốn! (Đang mượn: " + dangMuon + ", muốn mượn thêm: " + muonMoi + ")";
        }

        // 4. Thực thi Transaction
        boolean ok = phieuMuonDAO.lapPhieuMuonTransaction(maDocGia, maNV, hanTra, dsSach);
        return ok ? "OK" : "Lập phiếu mượn thất bại (kiểm tra lại số lượng sách tồn kho)!";
    }

    public double tinhTienPhatVaTraSach(PhieuMuonDTO pm, List<ChiTietPhieuMuonDTO> dsTra) {
        LocalDate today = LocalDate.now();
        long soNgayTre = ChronoUnit.DAYS.between(pm.getHanTra(), today);
        double phatTre = soNgayTre > 0 ? soNgayTre * PHAT_TRE_MOI_NGAY : 0;

        double tongPhat = phatTre;
        for (ChiTietPhieuMuonDTO ct : dsTra) {
            double phatSach = 0;
            if ("Hư hỏng".equalsIgnoreCase(ct.getTinhTrangKhiTra())) {
                phatSach = PHAT_HU_HONG * ct.getSoLuong();
            } else if ("Mất sách".equalsIgnoreCase(ct.getTinhTrangKhiTra())) {
                phatSach = PHAT_MAT_SACH * ct.getSoLuong();
            }
            ct.setTienPhat(phatSach);
            tongPhat += phatSach;
        }

        boolean ok = phieuMuonDAO.traSachTransaction(pm.getMaPhieuMuon(), tongPhat, dsTra);
        return ok ? tongPhat : -1;
    }
}