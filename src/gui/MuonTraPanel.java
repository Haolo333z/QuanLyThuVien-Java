package gui;

import bus.DatTruocBUS;
import bus.DocGiaBUS;
import bus.MuonTraBUS;
import bus.SachBUS;
import dto.ChiTietPhieuMuonDTO;
import dto.DatTruocDTO;
import dto.DocGiaDTO;
import dto.NhanVienDTO;
import dto.PhieuMuonDTO;
import dto.QuyDinhDTO;
import dto.SachDTO;
import utils.PrintExportUtil;
import utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class MuonTraPanel extends JPanel {
    private MuonTraBUS muonTraBUS = new MuonTraBUS();
    private DocGiaBUS docGiaBUS = new DocGiaBUS();
    private SachBUS sachBUS = new SachBUS();
    private DatTruocBUS datTruocBUS = new DatTruocBUS();

    // Tab 1: Lập phiếu mượn
    private JComboBox<String> cboDocGia, cboSach;
    private List<DocGiaDTO> listDocGia = new ArrayList<>();
    private List<SachDTO> listSach = new ArrayList<>();
    private JSpinner spnSoLuong;
    private JTextField txtHanTra;
    private JLabel lblGioSummary;
    private DefaultTableModel gioMuonModel;
    private List<ChiTietPhieuMuonDTO> gioMuon = new ArrayList<>();

    // Tab 2: Trả sách & Gia hạn
    private JTable tblPhieuMuon, tblChiTietTra;
    private DefaultTableModel phieuModel, chiTietModel;
    private List<PhieuMuonDTO> listPhieuDangMuon = new ArrayList<>();
    private List<ChiTietPhieuMuonDTO> listChiTietHienTai = new ArrayList<>();
    private TitledBorder spCTBorder;
    private JLabel lblDuKienPhat;

    // Tab 3: Lịch sử Mượn - Trả
    private JTextField txtTimLichSu;
    private JComboBox<String> cboLocTrangThai;
    private JTable tblLichSu, tblChiTietLichSu;
    private DefaultTableModel lichSuModel, chiTietLichSuModel;

    // Tab 4: Đặt trước sách
    private JComboBox<String> cboDocGiaDat, cboSachDat, cboLocDatTruoc;
    private JTextField txtGhiChuDat, txtTimDatTruoc;
    private JTable tblDatTruoc;
    private DefaultTableModel datTruocModel;

    public MuonTraPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 250, 252));

        JLabel lblTitle = new JLabel("QUẢN LÝ MƯỢN / TRẢ SÁCH");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));
        add(lblTitle, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.addTab("Lập phiếu mượn", createMuonTab());
        tabbedPane.addTab("Trả sách & Tính phạt", createTraTab());
        tabbedPane.addTab("Lịch sử mượn / trả", createLichSuTab());
        tabbedPane.addTab("Đặt trước sách", createDatTruocTab());

        // Tự động reload dữ liệu mới nhất khi chuyển tab
        tabbedPane.addChangeListener(e -> refreshData());

        add(tabbedPane, BorderLayout.CENTER);
        refreshData();
    }

    private JPanel createMuonTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new TitledBorder("Thông tin phiếu mượn"));

        QuyDinhDTO qd = muonTraBUS.getQuyDinhHienTai();
        int soNgayMacDinh = qd.getSoNgayMuonMacDinh() > 0 ? qd.getSoNgayMuonMacDinh() : 14;
        int maxSl = qd.getSoSachToiDa() > 0 ? qd.getSoSachToiDa() : MuonTraBUS.MAX_SACH_MUON;

        cboDocGia = new JComboBox<>();
        cboSach = new JComboBox<>();
        spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, maxSl, 1));
        txtHanTra = new JTextField(LocalDate.now().plusDays(soNgayMacDinh).toString());

        JButton btnThemVaoGio = createBtn("Thêm vào danh sách", new Color(37, 99, 235));
        JButton btnXoaDong = createBtn("Xóa dòng chọn", new Color(234, 88, 12));
        JButton btnXoaGio = createBtn("Xóa danh sách", new Color(100, 116, 139));
        JButton btnTaiLaiDuLieu = createBtn("Làm mới DS", new Color(14, 116, 144));

        JPanel cartBtnGroup = new JPanel(new GridLayout(1, 2, 8, 0));
        cartBtnGroup.setOpaque(false);
        cartBtnGroup.add(btnXoaDong);
        cartBtnGroup.add(btnXoaGio);

        formPanel.add(new JLabel("Chọn độc giả:"));          formPanel.add(cboDocGia);
        formPanel.add(new JLabel("Hạn trả (yyyy-MM-dd):")); formPanel.add(txtHanTra);
        formPanel.add(new JLabel("Chọn sách:"));             formPanel.add(cboSach);
        formPanel.add(new JLabel("Số lượng mượn:"));         formPanel.add(spnSoLuong);
        formPanel.add(btnTaiLaiDuLieu);                      formPanel.add(cartBtnGroup);
        formPanel.add(new JLabel(""));                       formPanel.add(btnThemVaoGio);

        gioMuonModel = new DefaultTableModel(new String[]{"Mã sách", "Tên sách", "Số lượng"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblGio = new JTable(gioMuonModel);
        tblGio.setRowHeight(28);
        tblGio.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        lblGioSummary = new JLabel("Tổng sách trong giỏ: 0 cuốn");
        lblGioSummary.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGioSummary.setForeground(new Color(30, 41, 59));

        JButton btnLapPhieu = createBtn("XÁC NHẬN LẬP PHIẾU MƯỢN", new Color(22, 163, 74));
        btnLapPhieu.setPreferredSize(new Dimension(0, 42));

        JPanel southMuonPanel = new JPanel(new BorderLayout(0, 8));
        southMuonPanel.setBackground(Color.WHITE);
        southMuonPanel.add(lblGioSummary, BorderLayout.NORTH);
        southMuonPanel.add(btnLapPhieu, BorderLayout.SOUTH);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblGio), BorderLayout.CENTER);
        panel.add(southMuonPanel, BorderLayout.SOUTH);

        btnTaiLaiDuLieu.addActionListener(e -> {
            refreshData();
            QuyDinhDTO currentQd = muonTraBUS.getQuyDinhHienTai();
            int days = currentQd.getSoNgayMuonMacDinh() > 0 ? currentQd.getSoNgayMuonMacDinh() : 14;
            txtHanTra.setText(LocalDate.now().plusDays(days).toString());
        });

        btnXoaDong.addActionListener(e -> {
            int row = tblGio.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng sách trong danh sách để xóa!");
                return;
            }
            int modelRow = tblGio.convertRowIndexToModel(row);
            if (modelRow >= 0 && modelRow < gioMuon.size()) {
                gioMuon.remove(modelRow);
                gioMuonModel.removeRow(modelRow);
                capNhatThongTinGioMuon();
            }
        });

        btnXoaGio.addActionListener(e -> {
            gioMuon.clear();
            gioMuonModel.setRowCount(0);
            capNhatThongTinGioMuon();
        });

        btnThemVaoGio.addActionListener(e -> {
            int idx = cboSach.getSelectedIndex();
            if (idx < 0 || idx >= listSach.size()) return;
            SachDTO s = listSach.get(idx);
            int sl = (int) spnSoLuong.getValue();

            if (sl > s.getSoLuongConLai()) {
                JOptionPane.showMessageDialog(this, "Sách này chỉ còn " + s.getSoLuongConLai() + " cuốn trong kho!");
                return;
            }
            for (ChiTietPhieuMuonDTO ct : gioMuon) {
                if (ct.getMaSach() == s.getMaSach()) {
                    JOptionPane.showMessageDialog(this, "Sách này đã có trong danh sách mượn!");
                    return;
                }
            }

            QuyDinhDTO currentQd = muonTraBUS.getQuyDinhHienTai();
            int maxAllowed = currentQd.getSoSachToiDa() > 0 ? currentQd.getSoSachToiDa() : MuonTraBUS.MAX_SACH_MUON;
            int tongTrongGio = gioMuon.stream().mapToInt(ChiTietPhieuMuonDTO::getSoLuong).sum();
            if (tongTrongGio + sl > maxAllowed) {
                JOptionPane.showMessageDialog(this,
                        "Tổng số lượng trong giỏ (" + (tongTrongGio + sl) + ") vượt quá giới hạn tối đa " + maxAllowed + " cuốn!",
                        "Vượt giới hạn mượn", JOptionPane.WARNING_MESSAGE);
                return;
            }

            gioMuon.add(new ChiTietPhieuMuonDTO(0, 0, s.getMaSach(), s.getTenSach(), sl, "Bình thường", null, 0));
            gioMuonModel.addRow(new Object[]{s.getMaSach(), s.getTenSach(), sl});
            capNhatThongTinGioMuon();
        });

        btnLapPhieu.addActionListener(e -> {
            int dgIdx = cboDocGia.getSelectedIndex();
            if (dgIdx < 0 || dgIdx >= listDocGia.size()) return;
            NhanVienDTO currentUser = Session.getCurrentUser();
            if (currentUser == null || currentUser.getMaNV() <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Không xác định được nhân viên đang đăng nhập! Vui lòng đăng nhập lại.",
                        "Lỗi phiên đăng nhập", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                LocalDate hanTra = LocalDate.parse(txtHanTra.getText().trim());
                int maDG = listDocGia.get(dgIdx).getMaDocGia();
                String msg = muonTraBUS.lapPhieuMuon(maDG, currentUser.getMaNV(), hanTra, gioMuon);
                if (msg != null && msg.startsWith("OK:")) {
                    int maPhieuMoi = Integer.parseInt(msg.substring(3));
                    gioMuon.clear();
                    gioMuonModel.setRowCount(0);
                    refreshData();

                    int printOpt = JOptionPane.showConfirmDialog(
                            this,
                            "Lập phiếu mượn #" + maPhieuMoi + " thành công và đã trừ tồn kho!\nBạn có muốn xem và in phiếu mượn ngay không?",
                            "Thành công",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    if (printOpt == JOptionPane.YES_OPTION) {
                        inPhieuTheoId(maPhieuMoi, false);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, msg, "Không thể lập phiếu", JOptionPane.WARNING_MESSAGE);
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Hạn trả phải đúng định dạng yyyy-MM-dd!", "Lỗi ngày", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createTraTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(Color.WHITE);

        // Bảng trên: Danh sách phiếu đang mượn
        phieuModel = new DefaultTableModel(
                new String[]{"Mã phiếu", "Độc giả", "Nhân viên", "Ngày mượn", "Hạn trả", "Đã gia hạn", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPhieuMuon = new JTable(phieuModel);
        tblPhieuMuon.setRowHeight(28);
        tblPhieuMuon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane spPhieu = new JScrollPane(tblPhieuMuon);
        spPhieu.setBorder(new TitledBorder("Danh sách phiếu đang mượn / quá hạn (Click chọn 1 phiếu để trả hoặc gia hạn)"));

        // Bảng dưới: Chi tiết sách cần trả (Cho phép chọn Tình trạng trả)
        chiTietModel = new DefaultTableModel(
                new String[]{"Mã CT", "Mã sách", "Tên sách", "Số lượng", "Tình trạng khi trả (Click để đổi)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        tblChiTietTra = new JTable(chiTietModel);
        tblChiTietTra.setRowHeight(28);
        JComboBox<String> cboTinhTrang = new JComboBox<>(new String[]{"Bình thường", "Hư hỏng", "Mất sách"});
        tblChiTietTra.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(cboTinhTrang));
        JScrollPane spCT = new JScrollPane(tblChiTietTra);
        spCTBorder = new TitledBorder("Chi tiết sách trả");
        spCT.setBorder(spCTBorder);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spPhieu, spCT);
        split.setDividerLocation(220);

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(new Color(254, 249, 195));
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(250, 204, 21)),
                new EmptyBorder(8, 12, 8, 12)
        ));
        lblDuKienPhat = new JLabel("Chọn một phiếu mượn ở trên để xem dự kiến tiền phạt.");
        lblDuKienPhat.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDuKienPhat.setForeground(new Color(146, 64, 14));
        previewPanel.add(lblDuKienPhat, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        actionPanel.setBackground(Color.WHITE);

        JButton btnGiaHanPhieu = createBtn("GIA HẠN PHIẾU MƯỢN", new Color(37, 99, 235));
        btnGiaHanPhieu.setPreferredSize(new Dimension(0, 42));

        JButton btnInPhieuDangMuon = createBtn("XEM & IN PHIẾU MƯỢN", new Color(14, 116, 144));
        btnInPhieuDangMuon.setPreferredSize(new Dimension(0, 42));

        JButton btnXacNhanTra = createBtn("XÁC NHẬN TRẢ SÁCH & TÍNH TIỀN PHẠT", new Color(234, 88, 12));
        btnXacNhanTra.setPreferredSize(new Dimension(0, 42));

        actionPanel.add(btnGiaHanPhieu);
        actionPanel.add(btnInPhieuDangMuon);
        actionPanel.add(btnXacNhanTra);

        JPanel southTraPanel = new JPanel(new BorderLayout(0, 10));
        southTraPanel.setBackground(Color.WHITE);
        southTraPanel.add(previewPanel, BorderLayout.NORTH);
        southTraPanel.add(actionPanel, BorderLayout.SOUTH);

        panel.add(split, BorderLayout.CENTER);
        panel.add(southTraPanel, BorderLayout.SOUTH);

        tblPhieuMuon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblPhieuMuon.getSelectedRow();
                if (row == -1) {
                    listChiTietHienTai.clear();
                    chiTietModel.setRowCount(0);
                    capNhatDuKienPhat();
                    return;
                }
                int modelRow = tblPhieuMuon.convertRowIndexToModel(row);
                if (modelRow >= 0 && modelRow < listPhieuDangMuon.size()) {
                    int maPM = listPhieuDangMuon.get(modelRow).getMaPhieuMuon();
                    listChiTietHienTai = muonTraBUS.getChiTiet(maPM);
                    chiTietModel.setRowCount(0);
                    for (ChiTietPhieuMuonDTO ct : listChiTietHienTai) {
                        chiTietModel.addRow(new Object[]{
                                ct.getMaChiTiet(), ct.getMaSach(), ct.getTenSach(), ct.getSoLuong(), "Bình thường"
                        });
                    }
                    capNhatDuKienPhat();
                }
            }
        });

        chiTietModel.addTableModelListener(e -> {
            if (e.getColumn() == 4) {
                capNhatDuKienPhat();
            }
        });

        btnGiaHanPhieu.addActionListener(e -> {
            PhieuMuonDTO pm = getSelectedActivePhieu();
            if (pm == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu mượn cần gia hạn!");
                return;
            }
            String res = muonTraBUS.giaHanPhieuMuon(pm);
            if (res != null && res.startsWith("OK:")) {
                String hanMoi = res.substring(3);
                JOptionPane.showMessageDialog(this,
                        "Gia hạn phiếu #" + pm.getMaPhieuMuon() + " thành công!\nHạn trả mới: " + hanMoi,
                        "Gia hạn thành công", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this,
                        res != null ? res : "Không thể gia hạn phiếu mượn!",
                        "Không thể gia hạn", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnInPhieuDangMuon.addActionListener(e -> {
            PhieuMuonDTO pm = getSelectedActivePhieu();
            if (pm == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu mượn để xem & in!");
                return;
            }
            inPhieuTheoId(pm.getMaPhieuMuon(), false);
        });

        btnXacNhanTra.addActionListener(e -> {
            PhieuMuonDTO pm = getSelectedActivePhieu();
            if (pm == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu mượn cần trả!");
                return;
            }
            if (listChiTietHienTai == null || listChiTietHienTai.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có chi tiết sách để trả!");
                return;
            }
            if (tblChiTietTra.isEditing()) {
                tblChiTietTra.getCellEditor().stopCellEditing();
            }

            dongBoTinhTrangTraVaoList();
            MuonTraBUS.DuKienPhat duKien = muonTraBUS.tinhDuKienTienPhat(pm, listChiTietHienTai);

            String confirmMsg = String.format(
                    "Xác nhận hoàn tất trả sách cho Phiếu #%d?\n"
                    + "- Độc giả: %s\n"
                    + "- Số ngày trễ: %d ngày (Phạt trễ: %,.0f VNĐ)\n"
                    + "- Phạt hư hỏng / mất sách: %,.0f VNĐ\n"
                    + "=> TỔNG TIỀN PHẠT PHẢI THU: %,.0f VNĐ",
                    pm.getMaPhieuMuon(),
                    pm.getTenDocGia(),
                    duKien.getSoNgayTre(),
                    duKien.getPhatTre(),
                    duKien.getPhatTinhTrang(),
                    duKien.getTongPhat()
            );

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    confirmMsg,
                    "Xác nhận trả sách",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            double tongPhat = muonTraBUS.tinhTienPhatVaTraSach(pm, listChiTietHienTai);
            if (tongPhat >= 0) {
                int maPM = pm.getMaPhieuMuon();
                List<String> notices = muonTraBUS.getLastReturnReservationNotices();
                chiTietModel.setRowCount(0);
                listChiTietHienTai.clear();
                refreshData();

                if (!notices.isEmpty()) {
                    StringBuilder noticeMsg = new StringBuilder(
                            "LƯU Ý ĐẶT TRƯỚC: Sách vừa trả đang có độc giả đặt trước (đã chuyển sang 'Đã có sách', giữ chỗ 3 ngày):\n"
                    );
                    for (String n : notices) {
                        noticeMsg.append("• ").append(n).append("\n");
                    }
                    JOptionPane.showMessageDialog(this, noticeMsg.toString(),
                            "Thông báo giữ chỗ đặt trước", JOptionPane.INFORMATION_MESSAGE);
                }

                int printOpt = JOptionPane.showConfirmDialog(
                        this,
                        String.format("Trả sách thành công!\nTổng tiền phạt đã ghi nhận: %,.0f VNĐ\n\nBạn có muốn xem và in biên lai trả sách không?", tongPhat),
                        "Hoàn tất trả sách",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                );
                if (printOpt == JOptionPane.YES_OPTION) {
                    inPhieuTheoId(maPM, true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi cập nhật trả sách (phiếu có thể đã được trả trước đó)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createLichSuTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(Color.WHITE);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(new TitledBorder("Tra cứu & lọc phiếu mượn"));

        txtTimLichSu = new JTextField(22);
        txtTimLichSu.putClientProperty("JTextField.placeholderText", "Mã phiếu, tên độc giả, SĐT...");
        cboLocTrangThai = new JComboBox<>(new String[]{"Tất cả", "Đang mượn", "Quá hạn", "Đã trả"});
        JButton btnTimLichSu = createBtn("Tìm kiếm", new Color(37, 99, 235));
        JButton btnTaiLaiLichSu = createBtn("Tải lại", new Color(100, 116, 139));

        filterPanel.add(new JLabel("Từ khóa:"));
        filterPanel.add(txtTimLichSu);
        filterPanel.add(new JLabel("Trạng thái:"));
        filterPanel.add(cboLocTrangThai);
        filterPanel.add(btnTimLichSu);
        filterPanel.add(btnTaiLaiLichSu);

        lichSuModel = new DefaultTableModel(
                new String[]{"Mã phiếu", "Độc giả", "Nhân viên lập", "Ngày mượn", "Hạn trả", "Ngày trả thực tế", "Gia hạn", "Tiền phạt (VNĐ)", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblLichSu = new JTable(lichSuModel);
        tblLichSu.setRowHeight(28);
        tblLichSu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane spLichSu = new JScrollPane(tblLichSu);
        spLichSu.setBorder(new TitledBorder("Danh sách phiếu mượn - trả (Click chọn để xem chi tiết)"));

        chiTietLichSuModel = new DefaultTableModel(
                new String[]{"Mã CT", "Mã sách", "Tên sách", "Số lượng", "Tình trạng mượn", "Tình trạng trả", "Tiền phạt (VNĐ)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblChiTietLichSu = new JTable(chiTietLichSuModel);
        tblChiTietLichSu.setRowHeight(28);
        JScrollPane spChiTietLS = new JScrollPane(tblChiTietLichSu);
        spChiTietLS.setBorder(new TitledBorder("Chi tiết đầu sách của phiếu đã chọn"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spLichSu, spChiTietLS);
        split.setDividerLocation(260);

        JButton btnInLichSu = createBtn("XEM & IN PHIẾU / BIÊN LAI ĐÃ CHỌN", new Color(14, 116, 144));
        btnInLichSu.setPreferredSize(new Dimension(0, 40));

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        panel.add(btnInLichSu, BorderLayout.SOUTH);

        btnInLichSu.addActionListener(e -> {
            int row = tblLichSu.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu trong danh sách lịch sử để in!");
                return;
            }
            int modelRow = tblLichSu.convertRowIndexToModel(row);
            int maPM = (int) lichSuModel.getValueAt(modelRow, 0);
            String trangThai = String.valueOf(lichSuModel.getValueAt(modelRow, 8));
            inPhieuTheoId(maPM, "Đã trả".equalsIgnoreCase(trangThai));
        });

        tblLichSu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblLichSu.getSelectedRow() != -1) {
                int modelRow = tblLichSu.convertRowIndexToModel(tblLichSu.getSelectedRow());
                int maPM = (int) lichSuModel.getValueAt(modelRow, 0);
                List<ChiTietPhieuMuonDTO> details = muonTraBUS.getChiTiet(maPM);
                chiTietLichSuModel.setRowCount(0);
                for (ChiTietPhieuMuonDTO ct : details) {
                    chiTietLichSuModel.addRow(new Object[]{
                            ct.getMaChiTiet(),
                            ct.getMaSach(),
                            ct.getTenSach(),
                            ct.getSoLuong(),
                            ct.getTinhTrangKhiMuon(),
                            ct.getTinhTrangKhiTra() != null ? ct.getTinhTrangKhiTra() : "Chưa trả",
                            String.format("%,.0f", ct.getTienPhat())
                    });
                }
            }
        });

        btnTimLichSu.addActionListener(e -> loadLichSuPhieu());
        cboLocTrangThai.addActionListener(e -> loadLichSuPhieu());
        btnTaiLaiLichSu.addActionListener(e -> {
            txtTimLichSu.setText("");
            cboLocTrangThai.setSelectedIndex(0);
            loadLichSuPhieu();
        });

        return panel;
    }

    private JPanel createDatTruocTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new TitledBorder("Đăng ký đặt trước sách (Dành cho đầu sách tạm hết tồn kho)"));

        cboDocGiaDat = new JComboBox<>();
        cboSachDat = new JComboBox<>();
        txtGhiChuDat = new JTextField();
        txtGhiChuDat.putClientProperty("JTextField.placeholderText", "VD: Gọi SĐT khi có sách...");
        JButton btnDangKyDat = createBtn("ĐĂNG KÝ ĐẶT TRƯỚC", new Color(22, 163, 74));

        formPanel.add(new JLabel("Chọn độc giả:"));   formPanel.add(cboDocGiaDat);
        formPanel.add(new JLabel("Chọn đầu sách:"));  formPanel.add(cboSachDat);
        formPanel.add(new JLabel("Ghi chú:"));        formPanel.add(txtGhiChuDat);
        formPanel.add(new JLabel(""));                formPanel.add(btnDangKyDat);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterPanel.setBackground(Color.WHITE);
        txtTimDatTruoc = new JTextField(20);
        txtTimDatTruoc.putClientProperty("JTextField.placeholderText", "Tên ĐG, SĐT, tên sách...");
        cboLocDatTruoc = new JComboBox<>(new String[]{"Tất cả", "Đã có sách", "Đang chờ", "Đã nhận", "Đã hủy"});
        JButton btnTimDat = createBtn("Tìm kiếm", new Color(37, 99, 235));
        JButton btnTaiLaiDat = createBtn("Tải lại", new Color(100, 116, 139));

        filterPanel.add(new JLabel("Từ khóa:"));
        filterPanel.add(txtTimDatTruoc);
        filterPanel.add(new JLabel("Trạng thái:"));
        filterPanel.add(cboLocDatTruoc);
        filterPanel.add(btnTimDat);
        filterPanel.add(btnTaiLaiDat);

        datTruocModel = new DefaultTableModel(
                new String[]{"Mã ĐT", "Độc giả", "SĐT", "Tên sách", "Ngày đặt", "Hạn giữ chỗ", "Trạng thái", "Ghi chú"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDatTruoc = new JTable(datTruocModel);
        tblDatTruoc.setRowHeight(28);
        tblDatTruoc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel centerBox = new JPanel(new BorderLayout(0, 8));
        centerBox.setBackground(Color.WHITE);
        centerBox.setBorder(new TitledBorder("Danh sách xếp hàng đặt trước (Ưu tiên FIFO khi trả sách, giữ chỗ 3 ngày)"));
        centerBox.add(filterPanel, BorderLayout.NORTH);
        centerBox.add(new JScrollPane(tblDatTruoc), BorderLayout.CENTER);

        JButton btnHuyDat = createBtn("HỦY ĐẶT TRƯỚC ĐÃ CHỌN", new Color(220, 38, 38));
        btnHuyDat.setPreferredSize(new Dimension(0, 40));

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(centerBox, BorderLayout.CENTER);
        panel.add(btnHuyDat, BorderLayout.SOUTH);

        btnDangKyDat.addActionListener(e -> {
            int dgIdx = cboDocGiaDat.getSelectedIndex();
            int sachIdx = cboSachDat.getSelectedIndex();
            if (dgIdx < 0 || dgIdx >= listDocGia.size() || sachIdx < 0 || sachIdx >= listSach.size()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ Độc giả và Đầu sách!");
                return;
            }
            NhanVienDTO currentUser = Session.getCurrentUser();
            if (currentUser == null || currentUser.getMaNV() <= 0) {
                JOptionPane.showMessageDialog(this, "Không xác định được nhân viên đăng nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int maDG = listDocGia.get(dgIdx).getMaDocGia();
            int maSach = listSach.get(sachIdx).getMaSach();
            String msg = datTruocBUS.datTruocSach(maDG, maSach, currentUser.getMaNV(), txtGhiChuDat.getText());
            if ("OK".equals(msg)) {
                JOptionPane.showMessageDialog(this, "Đăng ký đặt trước sách thành công!");
                txtGhiChuDat.setText("");
                loadDatTruoc();
            } else {
                JOptionPane.showMessageDialog(this, msg, "Không thể đặt trước", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnHuyDat.addActionListener(e -> {
            int row = tblDatTruoc.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một yêu cầu đặt trước cần hủy!");
                return;
            }
            int modelRow = tblDatTruoc.convertRowIndexToModel(row);
            int maDT = (int) datTruocModel.getValueAt(modelRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xác nhận hủy yêu cầu đặt trước #" + maDT + "?",
                    "Xác nhận hủy", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String msg = datTruocBUS.huyDatTruoc(maDT);
                if ("OK".equals(msg)) {
                    JOptionPane.showMessageDialog(this, "Đã hủy yêu cầu đặt trước!");
                    loadDatTruoc();
                } else {
                    JOptionPane.showMessageDialog(this, msg, "Không thể hủy", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        btnTimDat.addActionListener(e -> loadDatTruoc());
        cboLocDatTruoc.addActionListener(e -> loadDatTruoc());
        btnTaiLaiDat.addActionListener(e -> {
            txtTimDatTruoc.setText("");
            cboLocDatTruoc.setSelectedIndex(0);
            loadDatTruoc();
        });

        return panel;
    }

    private void inPhieuTheoId(int maPM, boolean isReturnReceipt) {
        PhieuMuonDTO pm = muonTraBUS.getPhieuById(maPM);
        if (pm == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin phiếu #" + maPM + "!",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<ChiTietPhieuMuonDTO> details = muonTraBUS.getChiTiet(maPM);
        PrintExportUtil.showReceiptPreviewAndPrint(this, pm, details, isReturnReceipt);
    }

    private PhieuMuonDTO getSelectedActivePhieu() {
        int row = tblPhieuMuon.getSelectedRow();
        if (row == -1) {
            return null;
        }
        int modelRow = tblPhieuMuon.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= listPhieuDangMuon.size()) {
            return null;
        }
        return listPhieuDangMuon.get(modelRow);
    }

    private void dongBoTinhTrangTraVaoList() {
        if (listChiTietHienTai == null) {
            return;
        }
        int count = Math.min(listChiTietHienTai.size(), chiTietModel.getRowCount());
        for (int i = 0; i < count; i++) {
            Object val = chiTietModel.getValueAt(i, 4);
            listChiTietHienTai.get(i).setTinhTrangKhiTra(val != null ? val.toString() : "Bình thường");
        }
    }

    private void capNhatDuKienPhat() {
        if (lblDuKienPhat == null) {
            return;
        }
        PhieuMuonDTO pm = getSelectedActivePhieu();
        if (pm == null || listChiTietHienTai == null || listChiTietHienTai.isEmpty()) {
            lblDuKienPhat.setText("Chọn một phiếu mượn ở trên để xem dự kiến tiền phạt.");
            return;
        }
        dongBoTinhTrangTraVaoList();
        MuonTraBUS.DuKienPhat duKien = muonTraBUS.tinhDuKienTienPhat(pm, listChiTietHienTai);
        lblDuKienPhat.setText(String.format(
                "Phiếu #%d (%s)  |  Trễ hạn: %d ngày (%,.0f VNĐ)  |  Hư hỏng/Mất: %,.0f VNĐ  |  TỔNG PHẠT DỰ KIẾN: %,.0f VNĐ",
                pm.getMaPhieuMuon(),
                pm.getTenDocGia(),
                duKien.getSoNgayTre(),
                duKien.getPhatTre(),
                duKien.getPhatTinhTrang(),
                duKien.getTongPhat()
        ));
    }

    private void capNhatThongTinGioMuon() {
        if (lblGioSummary == null) {
            return;
        }
        QuyDinhDTO qd = muonTraBUS.getQuyDinhHienTai();
        int maxSl = qd.getSoSachToiDa() > 0 ? qd.getSoSachToiDa() : MuonTraBUS.MAX_SACH_MUON;
        int total = gioMuon.stream().mapToInt(ChiTietPhieuMuonDTO::getSoLuong).sum();
        lblGioSummary.setText("Tổng sách trong giỏ: " + total + " / " + maxSl + " cuốn tối đa");
    }

    public void refreshData() {
        loadComboBoxData();
        loadPhieuDangMuon();
        loadLichSuPhieu();
        loadDatTruoc();
        capNhatGhiChuQuyDinh();
        capNhatThongTinGioMuon();
        capNhatDuKienPhat();
    }

    private void capNhatGhiChuQuyDinh() {
        QuyDinhDTO qd = muonTraBUS.getQuyDinhHienTai();
        if (spnSoLuong != null && spnSoLuong.getModel() instanceof SpinnerNumberModel) {
            int maxSl = qd.getSoSachToiDa() > 0 ? qd.getSoSachToiDa() : MuonTraBUS.MAX_SACH_MUON;
            SpinnerNumberModel model = (SpinnerNumberModel) spnSoLuong.getModel();
            model.setMaximum(maxSl);
            if ((int) model.getValue() > maxSl) {
                model.setValue(maxSl);
            }
        }
        if (spCTBorder != null) {
            spCTBorder.setTitle(String.format(
                    "Chi tiết sách trả (Phạt trễ: %,.0fđ/ngày | Hư hỏng: %,.0fđ/cuốn | Mất sách: %,.0fđ/cuốn | Gia hạn +%d ngày)",
                    (double) qd.getPhatTreMoiNgay(),
                    (double) qd.getPhatHuHong(),
                    (double) qd.getPhatMatSach(),
                    qd.getSoNgayGiaHanMacDinh()
            ));
            repaint();
        }
    }

    private void loadComboBoxData() {
        cboDocGia.removeAllItems();
        if (cboDocGiaDat != null) cboDocGiaDat.removeAllItems();
        listDocGia = docGiaBUS.getAll();
        for (DocGiaDTO dg : listDocGia) {
            String item = dg.getMaDocGia() + " - " + dg.getHoTen() + (dg.isTrangThai() ? "" : " [Đã khóa]");
            cboDocGia.addItem(item);
            if (cboDocGiaDat != null) cboDocGiaDat.addItem(item);
        }

        cboSach.removeAllItems();
        if (cboSachDat != null) cboSachDat.removeAllItems();
        listSach.clear();
        for (SachDTO s : sachBUS.getAllSach()) {
            if (s.isTrangThai()) {
                listSach.add(s);
                cboSach.addItem(s.getMaSach() + " - " + s.getTenSach() + " (Còn: " + s.getSoLuongConLai() + ")");
                if (cboSachDat != null) {
                    String status = s.getSoLuongConLai() == 0
                            ? " [HẾT SÁCH - Đặt trước]"
                            : " (Còn: " + s.getSoLuongConLai() + ")";
                    cboSachDat.addItem(s.getMaSach() + " - " + s.getTenSach() + status);
                }
            }
        }
    }

    private void loadDatTruoc() {
        if (datTruocModel == null) {
            return;
        }
        String keyword = txtTimDatTruoc != null ? txtTimDatTruoc.getText() : "";
        String trangThai = cboLocDatTruoc != null && cboLocDatTruoc.getSelectedItem() != null
                ? cboLocDatTruoc.getSelectedItem().toString()
                : "Tất cả";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        datTruocModel.setRowCount(0);
        for (DatTruocDTO dt : datTruocBUS.search(keyword, trangThai)) {
            datTruocModel.addRow(new Object[]{
                    dt.getMaDatTruoc(),
                    dt.getTenDocGia(),
                    dt.getSdtDocGia(),
                    dt.getTenSach(),
                    dt.getNgayDat() != null ? dt.getNgayDat().format(fmt) : "",
                    dt.getHanGiuCho() != null ? dt.getHanGiuCho().toString() : "-",
                    dt.getTrangThai(),
                    dt.getGhiChu() != null ? dt.getGhiChu() : ""
            });
        }
    }

    private void loadPhieuDangMuon() {
        phieuModel.setRowCount(0);
        listPhieuDangMuon = muonTraBUS.getPhieuDangMuon();
        for (PhieuMuonDTO pm : listPhieuDangMuon) {
            phieuModel.addRow(new Object[]{
                    pm.getMaPhieuMuon(), pm.getTenDocGia(), pm.getTenNV(),
                    pm.getNgayMuon(), pm.getHanTra(), pm.getSoLanGiaHan(), pm.getTrangThai()
            });
        }
        chiTietModel.setRowCount(0);
        listChiTietHienTai.clear();
    }

    private void loadLichSuPhieu() {
        if (lichSuModel == null) {
            return;
        }
        String keyword = txtTimLichSu != null ? txtTimLichSu.getText() : "";
        String trangThai = cboLocTrangThai != null && cboLocTrangThai.getSelectedItem() != null
                ? cboLocTrangThai.getSelectedItem().toString()
                : "Tất cả";

        lichSuModel.setRowCount(0);
        if (chiTietLichSuModel != null) {
            chiTietLichSuModel.setRowCount(0);
        }
        List<PhieuMuonDTO> ds = muonTraBUS.searchPhieuMuon(keyword, trangThai);
        for (PhieuMuonDTO pm : ds) {
            lichSuModel.addRow(new Object[]{
                    pm.getMaPhieuMuon(),
                    pm.getTenDocGia(),
                    pm.getTenNV(),
                    pm.getNgayMuon(),
                    pm.getHanTra(),
                    pm.getNgayTraThucTe() != null ? pm.getNgayTraThucTe().toString() : "-",
                    pm.getSoLanGiaHan(),
                    String.format("%,.0f", pm.getTongTienPhat()),
                    pm.getTrangThai()
            });
        }
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
