package bus;

import dao.DocGiaDAO;
import dao.PhieuMuonDAO;
import dao.QuyDinhDAO;
import dto.ChiTietPhieuMuonDTO;
import dto.DocGiaDTO;
import dto.PhieuMuonDTO;
import dto.QuyDinhDTO;

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
    private QuyDinhDAO quyDinhDAO = new QuyDinhDAO();

    public QuyDinhDTO getQuyDinhHienTai() {
        QuyDinhDTO qd = quyDinhDAO.getQuyDinh();
        return qd != null ? qd : new QuyDinhDTO();
    }

    public List<PhieuMuonDTO> getPhieuDangMuon() {
        return phieuMuonDAO.getAll(true);
    }

    public List<ChiTietPhieuMuonDTO> getChiTiet(int maPhieuMuon) {
        return phieuMuonDAO.getChiTietByPhieu(maPhieuMuon);
    }

    public String lapPhieuMuon(int maDocGia, int maNV, LocalDate hanTra, List<ChiTietPhieuMuonDTO> dsSach) {
        if (maNV <= 0) {
            return "Không xác định được nhân viên lập phiếu (vui lòng đăng nhập lại)!";
        }
        if (dsSach == null || dsSach.isEmpty()) {
            return "Vui lòng chọn ít nhất 1 cuốn sách để mượn!";
        }
        java.util.Set<Integer> seenMaSach = new java.util.HashSet<>();
        for (ChiTietPhieuMuonDTO ct : dsSach) {
            if (ct.getSoLuong() <= 0) {
                return "Số lượng mượn của mỗi đầu sách phải lớn hơn 0!";
            }
            if (!seenMaSach.add(ct.getMaSach())) {
                return "Danh sách mượn bị trùng đầu sách (Mã sách: " + ct.getMaSach() + ")!";
            }
        }
        if (hanTra == null || hanTra.isBefore(LocalDate.now())) {
            return "Hạn trả không được nhỏ hơn ngày hiện tại!";
        }

        QuyDinhDTO qd = getQuyDinhHienTai();
        int maxSachMuon = qd.getSoSachToiDa() > 0 ? qd.getSoSachToiDa() : MAX_SACH_MUON;

        // 1. Kiểm tra thẻ độc giả
        DocGiaDTO dg = docGiaDAO.getById(maDocGia);
        if (dg == null) return "Không tìm thấy thông tin độc giả!";
        if (!dg.isTrangThai()) return "Thẻ độc giả đang bị KHÓA, không thể mượn sách!";
        if (dg.getNgayHetHan().isBefore(LocalDate.now())) {
            return "Thẻ độc giả đã HẾT HẠN (" + dg.getNgayHetHan() + "), vui lòng gia hạn trước khi mượn!";
        }
        if (hanTra.isAfter(dg.getNgayHetHan())) {
            return "Hạn trả (" + hanTra + ") không được vượt quá ngày hết hạn thẻ độc giả (" + dg.getNgayHetHan() + ")!";
        }

        // 2. Kiểm tra có phiếu quá hạn chưa trả không
        if (phieuMuonDAO.hasPhieuQuaHan(maDocGia)) {
            return "Độc giả đang có phiếu mượn QUÁ HẠN chưa trả, không được mượn thêm!";
        }

        // 3. Kiểm tra giới hạn số cuốn tối đa theo quy định
        int dangMuon = phieuMuonDAO.countSachDangMuon(maDocGia);
        int muonMoi = dsSach.stream().mapToInt(ChiTietPhieuMuonDTO::getSoLuong).sum();
        if (dangMuon + muonMoi > maxSachMuon) {
            return "Vượt quá giới hạn tối đa " + maxSachMuon + " cuốn! (Đang mượn: " + dangMuon + ", muốn mượn thêm: " + muonMoi + ")";
        }

        // 4. Thực thi Transaction
        boolean ok = phieuMuonDAO.lapPhieuMuonTransaction(maDocGia, maNV, hanTra, dsSach);
        return ok ? "OK" : "Lập phiếu mượn thất bại (kiểm tra lại số lượng sách tồn kho)!";
    }

    public String giaHanPhieuMuon(PhieuMuonDTO pm) {
        if (pm == null) {
            return "Vui lòng chọn phiếu mượn cần gia hạn!";
        }
        if ("Đã trả".equalsIgnoreCase(pm.getTrangThai())) {
            return "Phiếu mượn này đã trả xong, không thể gia hạn!";
        }

        LocalDate today = LocalDate.now();
        if (pm.getHanTra() != null && pm.getHanTra().isBefore(today)) {
            return "Phiếu mượn đã QUÁ HẠN (" + pm.getHanTra() + "), không thể gia hạn. Vui lòng thực hiện trả sách và xử lý phạt!";
        }

        DocGiaDTO dg = docGiaDAO.getById(pm.getMaDocGia());
        if (dg == null) return "Không tìm thấy thông tin độc giả!";
        if (!dg.isTrangThai()) return "Thẻ độc giả đang bị KHÓA, không thể gia hạn phiếu mượn!";

        QuyDinhDTO qd = getQuyDinhHienTai();
        int soNgayGiaHan = qd.getSoNgayGiaHanMacDinh() > 0 ? qd.getSoNgayGiaHanMacDinh() : 7;
        int soLanToiDa = qd.getSoLanGiaHanToiDa() >= 0 ? qd.getSoLanGiaHanToiDa() : 1;

        if (pm.getSoLanGiaHan() >= soLanToiDa) {
            return "Phiếu mượn đã đạt số lần gia hạn tối đa (" + soLanToiDa + " lần)!";
        }

        LocalDate baseDate = (pm.getHanTra() != null && pm.getHanTra().isAfter(today)) ? pm.getHanTra() : today;
        LocalDate hanTraMoi = baseDate.plusDays(soNgayGiaHan);

        if (dg.getNgayHetHan() != null && hanTraMoi.isAfter(dg.getNgayHetHan())) {
            return "Hạn trả mới (" + hanTraMoi + ") vượt quá hạn thẻ độc giả (" + dg.getNgayHetHan() + "). Vui lòng gia hạn thẻ độc giả trước!";
        }

        boolean ok = phieuMuonDAO.giaHanPhieuMuon(pm.getMaPhieuMuon(), hanTraMoi);
        return ok ? "OK:" + hanTraMoi : "Gia hạn phiếu mượn thất bại!";
    }

    public double tinhTienPhatVaTraSach(PhieuMuonDTO pm, List<ChiTietPhieuMuonDTO> dsTra) {
        if (pm == null || dsTra == null || dsTra.isEmpty() || "Đã trả".equalsIgnoreCase(pm.getTrangThai())) {
            return -1;
        }
        QuyDinhDTO qd = getQuyDinhHienTai();
        double phatTreMoiNgay = qd.getPhatTreMoiNgay() > 0 ? qd.getPhatTreMoiNgay() : PHAT_TRE_MOI_NGAY;
        double phatHuHong = qd.getPhatHuHong() > 0 ? qd.getPhatHuHong() : PHAT_HU_HONG;
        double phatMatSach = qd.getPhatMatSach() > 0 ? qd.getPhatMatSach() : PHAT_MAT_SACH;

        LocalDate today = LocalDate.now();
        long soNgayTre = ChronoUnit.DAYS.between(pm.getHanTra(), today);
        double phatTre = soNgayTre > 0 ? soNgayTre * phatTreMoiNgay : 0;

        double tongPhat = phatTre;
        for (ChiTietPhieuMuonDTO ct : dsTra) {
            double phatSach = 0;
            if ("Hư hỏng".equalsIgnoreCase(ct.getTinhTrangKhiTra())) {
                phatSach = phatHuHong * ct.getSoLuong();
            } else if ("Mất sách".equalsIgnoreCase(ct.getTinhTrangKhiTra())) {
                phatSach = phatMatSach * ct.getSoLuong();
            }
            ct.setTienPhat(phatSach);
            tongPhat += phatSach;
        }

        boolean ok = phieuMuonDAO.traSachTransaction(pm.getMaPhieuMuon(), tongPhat, dsTra);
        return ok ? tongPhat : -1;
    }
}