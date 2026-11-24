package gui;

import bus.DocGiaBUS;
import bus.MuonTraBUS;
import bus.SachBUS;
import dto.ChiTietPhieuMuonDTO;
import dto.DocGiaDTO;
import dto.PhieuMuonDTO;
import dto.SachDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MuonTraPanel extends JPanel {
    private MuonTraBUS muonTraBUS = new MuonTraBUS();
    private DocGiaBUS docGiaBUS = new DocGiaBUS();
    private SachBUS sachBUS = new SachBUS();

    // Tab 1: Lập phiếu mượn
    private JComboBox<String> cboDocGia, cboSach;
    private List<DocGiaDTO> listDocGia = new ArrayList<>();
    private List<SachDTO> listSach = new ArrayList<>();
    private JSpinner spnSoLuong;
    private JTextField txtHanTra;
    private DefaultTableModel gioMuonModel;
    private List<ChiTietPhieuMuonDTO> gioMuon = new ArrayList<>();

    // Tab 2: Trả sách
    private JTable tblPhieuMuon, tblChiTietTra;
    private DefaultTableModel phieuModel, chiTietModel;
    private List<PhieuMuonDTO> listPhieuDangMuon = new ArrayList<>();
    private List<ChiTietPhieuMuonDTO> listChiTietHienTai = new ArrayList<>();

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

        // Tự động reload dữ liệu mới nhất khi chuyển tab
        tabbedPane.addChangeListener(e -> {
            loadComboBoxData();
            loadPhieuDangMuon();
        });

        add(tabbedPane, BorderLayout.CENTER);
        loadComboBoxData();
        loadPhieuDangMuon();
    }

    private JPanel createMuonTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new TitledBorder("Thông tin phiếu mượn"));

        cboDocGia = new JComboBox<>();
        cboSach = new JComboBox<>();
        spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        txtHanTra = new JTextField(LocalDate.now().plusDays(14).toString());

        JButton btnThemVaoGio = createBtn("Thêm vào danh sách", new Color(37, 99, 235));
        JButton btnXoaGio = createBtn("Xóa danh sách", new Color(100, 116, 139));
        JButton btnTaiLaiDuLieu = createBtn("Làm mới DS", new Color(14, 116, 144));

        formPanel.add(new JLabel("Chọn độc giả:"));      formPanel.add(cboDocGia);
        formPanel.add(new JLabel("Hạn trả (yyyy-MM-dd):")); formPanel.add(txtHanTra);
        formPanel.add(new JLabel("Chọn sách:"));         formPanel.add(cboSach);
        formPanel.add(new JLabel("Số lượng mượn:"));     formPanel.add(spnSoLuong);
        formPanel.add(btnTaiLaiDuLieu);                  formPanel.add(btnXoaGio);
        formPanel.add(new JLabel(""));                   formPanel.add(btnThemVaoGio);

        gioMuonModel = new DefaultTableModel(new String[]{"Mã sách", "Tên sách", "Số lượng"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblGio = new JTable(gioMuonModel);
        tblGio.setRowHeight(28);

        JButton btnLapPhieu = createBtn("XÁC NHẬN LẬP PHIẾU MƯỢN", new Color(22, 163, 74));
        btnLapPhieu.setPreferredSize(new Dimension(0, 42));

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblGio), BorderLayout.CENTER);
        panel.add(btnLapPhieu, BorderLayout.SOUTH);

        btnTaiLaiDuLieu.addActionListener(e -> loadComboBoxData());
        btnXoaGio.addActionListener(e -> {
            gioMuon.clear();
            gioMuonModel.setRowCount(0);
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
            gioMuon.add(new ChiTietPhieuMuonDTO(0, 0, s.getMaSach(), s.getTenSach(), sl, "Bình thường", null, 0));
            gioMuonModel.addRow(new Object[]{s.getMaSach(), s.getTenSach(), sl});
        });

        btnLapPhieu.addActionListener(e -> {
            int dgIdx = cboDocGia.getSelectedIndex();
            if (dgIdx < 0 || dgIdx >= listDocGia.size()) return;
            try {
                LocalDate hanTra = LocalDate.parse(txtHanTra.getText().trim());
                int maDG = listDocGia.get(dgIdx).getMaDocGia();
                // MaNV = 1 (Admin mặc định)
                String msg = muonTraBUS.lapPhieuMuon(maDG, 1, hanTra, gioMuon);
                if ("OK".equals(msg)) {
                    JOptionPane.showMessageDialog(this, "Lập phiếu mượn thành công và đã trừ tồn kho!");
                    gioMuon.clear();
                    gioMuonModel.setRowCount(0);
                    loadComboBoxData();
                    loadPhieuDangMuon();
                } else {
                    JOptionPane.showMessageDialog(this, msg, "Không thể lập phiếu", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
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
        phieuModel = new DefaultTableModel(new String[]{"Mã phiếu", "Độc giả", "Nhân viên", "Ngày mượn", "Hạn trả", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPhieuMuon = new JTable(phieuModel);
        tblPhieuMuon.setRowHeight(28);
        JScrollPane spPhieu = new JScrollPane(tblPhieuMuon);
        spPhieu.setBorder(new TitledBorder("Danh sách phiếu đang mượn (Click chọn 1 phiếu)"));

        // Bảng dưới: Chi tiết sách cần trả (Cho phép chọn Tình trạng trả)
        chiTietModel = new DefaultTableModel(new String[]{"Mã CT", "Mã sách", "Tên sách", "Số lượng", "Tình trạng khi trả (Click để đổi)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        tblChiTietTra = new JTable(chiTietModel);
        tblChiTietTra.setRowHeight(28);
        JComboBox<String> cboTinhTrang = new JComboBox<>(new String[]{"Bình thường", "Hư hỏng", "Mất sách"});
        tblChiTietTra.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(cboTinhTrang));
        JScrollPane spCT = new JScrollPane(tblChiTietTra);
        spCT.setBorder(new TitledBorder("Chi tiết sách trả (Phạt trễ: 5.000đ/ngày | Hư hỏng: 50.000đ/cuốn | Mất sách: 150.000đ/cuốn)"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spPhieu, spCT);
        split.setDividerLocation(240);

        JButton btnXacNhanTra = createBtn("XÁC NHẬN TRẢ SÁCH & TÍNH TIỀN PHẠT", new Color(234, 88, 12));
        btnXacNhanTra.setPreferredSize(new Dimension(0, 42));

        panel.add(split, BorderLayout.CENTER);
        panel.add(btnXacNhanTra, BorderLayout.SOUTH);

        tblPhieuMuon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblPhieuMuon.getSelectedRow() != -1) {
                int maPM = (int) phieuModel.getValueAt(tblPhieuMuon.getSelectedRow(), 0);
                listChiTietHienTai = muonTraBUS.getChiTiet(maPM);
                chiTietModel.setRowCount(0);
                for (ChiTietPhieuMuonDTO ct : listChiTietHienTai) {
                    chiTietModel.addRow(new Object[]{
                            ct.getMaChiTiet(), ct.getMaSach(), ct.getTenSach(), ct.getSoLuong(), "Bình thường"
                    });
                }
            }
        });

        btnXacNhanTra.addActionListener(e -> {
            int row = tblPhieuMuon.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu mượn cần trả!");
                return;
            }
            if (tblChiTietTra.isEditing()) {
                tblChiTietTra.getCellEditor().stopCellEditing();
            }
            PhieuMuonDTO pm = listPhieuDangMuon.get(row);
            for (int i = 0; i < listChiTietHienTai.size(); i++) {
                String tt = chiTietModel.getValueAt(i, 4).toString();
                listChiTietHienTai.get(i).setTinhTrangKhiTra(tt);
            }
            double tongPhat = muonTraBUS.tinhTienPhatVaTraSach(pm, listChiTietHienTai);
            if (tongPhat >= 0) {
                JOptionPane.showMessageDialog(this,
                        String.format("Trả sách thành công!\nTổng tiền phạt: %,.0f VNĐ", tongPhat),
                        "Hoàn tất trả sách", JOptionPane.INFORMATION_MESSAGE);
                chiTietModel.setRowCount(0);
                loadPhieuDangMuon();
                loadComboBoxData();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi cập nhật trả sách!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private void loadComboBoxData() {
        cboDocGia.removeAllItems();
        listDocGia = docGiaBUS.getAll();
        for (DocGiaDTO dg : listDocGia) {
            cboDocGia.addItem(dg.getMaDocGia() + " - " + dg.getHoTen() + (dg.isTrangThai() ? "" : " [Đã khóa]"));
        }

        cboSach.removeAllItems();
        listSach.clear();
        for (SachDTO s : sachBUS.getAllSach()) {
            if (s.isTrangThai()) {
                listSach.add(s);
                cboSach.addItem(s.getMaSach() + " - " + s.getTenSach() + " (Còn: " + s.getSoLuongConLai() + ")");
            }
        }
    }

    private void loadPhieuDangMuon() {
        phieuModel.setRowCount(0);
        listPhieuDangMuon = muonTraBUS.getPhieuDangMuon();
        for (PhieuMuonDTO pm : listPhieuDangMuon) {
            phieuModel.addRow(new Object[]{
                    pm.getMaPhieuMuon(), pm.getTenDocGia(), pm.getTenNV(),
                    pm.getNgayMuon(), pm.getHanTra(), pm.getTrangThai()
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