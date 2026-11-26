package gui;

import bus.SachBUS;
import dto.SachDTO;
import dto.TheLoaiDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SachPanel extends JPanel {
    private SachBUS sachBUS = new SachBUS();

    private JTextField txtMaSach, txtTenSach, txtTacGia, txtNXB, txtNamXB, txtISBN, txtSoLuongTong, txtSoLuongCon, txtViTriKe, txtTimKiem;
    private JComboBox<TheLoaiDTO> cboTheLoai, cboLocTheLoai;
    private JComboBox<String> cboTinhTrang;
    private JCheckBox chkLuuHanh;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<SachDTO> currentList;

    public SachPanel() {
        initUI();
        loadTheLoai();
        loadData(sachBUS.getAllSach());
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 250, 252));

        // 1. HEADER & TÌM KIẾM NHIỀU TIÊU CHÍ
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ SÁCH & THỂ LOẠI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        cboLocTheLoai = new JComboBox<>();
        txtTimKiem = new JTextField(18);
        txtTimKiem.putClientProperty("JTextField.placeholderText", "Tên sách, tác giả, ISBN...");
        JButton btnTim = new JButton("Tìm kiếm");
        JButton btnTaiLai = new JButton("Tải lại");
        searchPanel.add(new JLabel("Thể loại:"));
        searchPanel.add(cboLocTheLoai);
        searchPanel.add(txtTimKiem);
        searchPanel.add(btnTim);
        searchPanel.add(btnTaiLai);

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        // 2. FORM NHẬP THÔNG TIN SÁCH
        JPanel formContainer = new JPanel(new BorderLayout(10, 10));
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("Thông tin đầu sách"),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JPanel fieldsPanel = new JPanel(new GridLayout(6, 4, 12, 8));
        fieldsPanel.setBackground(Color.WHITE);

        txtMaSach = new JTextField(); txtMaSach.setEditable(false);
        txtTenSach = new JTextField();
        cboTheLoai = new JComboBox<>();
        txtTacGia = new JTextField();
        txtNXB = new JTextField();
        txtNamXB = new JTextField("2024");
        txtISBN = new JTextField();
        txtSoLuongTong = new JTextField("1");
        txtSoLuongCon = new JTextField("1");
        txtViTriKe = new JTextField();
        cboTinhTrang = new JComboBox<>(new String[]{"Bình thường", "Cần kiểm tra", "Thanh lý"});
        cboTinhTrang.setBackground(Color.WHITE);
        chkLuuHanh = new JCheckBox("Đang lưu hành", true);
        chkLuuHanh.setBackground(Color.WHITE);

        fieldsPanel.add(new JLabel("Mã sách:"));          fieldsPanel.add(txtMaSach);
        fieldsPanel.add(new JLabel("Tên sách (*):"));     fieldsPanel.add(txtTenSach);
        fieldsPanel.add(new JLabel("Thể loại (*):"));     fieldsPanel.add(cboTheLoai);
        fieldsPanel.add(new JLabel("Tác giả (*):"));      fieldsPanel.add(txtTacGia);
        fieldsPanel.add(new JLabel("Nhà xuất bản:"));     fieldsPanel.add(txtNXB);
        fieldsPanel.add(new JLabel("Năm xuất bản:"));     fieldsPanel.add(txtNamXB);
        fieldsPanel.add(new JLabel("Mã ISBN:"));          fieldsPanel.add(txtISBN);
        fieldsPanel.add(new JLabel("Vị trí kệ:"));        fieldsPanel.add(txtViTriKe);
        fieldsPanel.add(new JLabel("Số lượng tổng:"));    fieldsPanel.add(txtSoLuongTong);
        fieldsPanel.add(new JLabel("Số lượng còn lại:")); fieldsPanel.add(txtSoLuongCon);
        fieldsPanel.add(new JLabel("Tình trạng sách:"));  fieldsPanel.add(cboTinhTrang);
        fieldsPanel.add(new JLabel("Trạng thái:"));       fieldsPanel.add(chkLuuHanh);

        // Các nút thao tác
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnThemTheLoai = createActionButton("+ Thêm thể loại", new Color(14, 116, 144));
        JButton btnThem = createActionButton("Thêm sách", new Color(22, 163, 74));
        JButton btnSua = createActionButton("Cập nhật", new Color(37, 99, 235));
        JButton btnThanhLy = createActionButton("Thanh lý sách", new Color(185, 28, 28));
        JButton btnXoaMem = createActionButton("Ẩn / Hiện sách", new Color(234, 88, 12));
        JButton btnLamMoi = createActionButton("Làm mới", new Color(100, 116, 139));

        buttonPanel.add(btnThemTheLoai);
        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnThanhLy);
        buttonPanel.add(btnXoaMem);
        buttonPanel.add(btnLamMoi);

        formContainer.add(fieldsPanel, BorderLayout.CENTER);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        // 3. BẢNG DANH SÁCH SÁCH
        String[] columns = {
                "Mã", "Tên sách", "Thể loại", "Tác giả", "NXB", "Năm XB",
                "ISBN", "Tổng", "Còn lại", "Đã thanh lý", "Tình trạng", "Vị trí kệ", "Trạng thái"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(formContainer, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // 4. SỰ KIỆN
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                txtMaSach.setText(val(r, 0));
                txtTenSach.setText(val(r, 1));
                selectTheLoaiByName(val(r, 2));
                txtTacGia.setText(val(r, 3));
                txtNXB.setText(val(r, 4));
                txtNamXB.setText(val(r, 5));
                txtISBN.setText(val(r, 6));
                txtSoLuongTong.setText(val(r, 7));
                txtSoLuongCon.setText(val(r, 8));
                selectTinhTrang(val(r, 10));
                txtViTriKe.setText(val(r, 11));
                chkLuuHanh.setSelected("Lưu hành".equals(val(r, 12)));
            }
        });

        btnThemTheLoai.addActionListener(e -> xuLyThemTheLoai());
        btnThem.addActionListener(e -> xuLyThemSach());
        btnSua.addActionListener(e -> xuLySuaSach());
        btnThanhLy.addActionListener(e -> xuLyThanhLySach());
        btnXoaMem.addActionListener(e -> xuLyAnHien());
        btnLamMoi.addActionListener(e -> clearForm());
        btnTim.addActionListener(e -> xuLyTimKiem());
        btnTaiLai.addActionListener(e -> {
            txtTimKiem.setText("");
            cboLocTheLoai.setSelectedIndex(0);
            loadData(sachBUS.getAllSach());
        });
    }

    private void loadTheLoai() {
        cboTheLoai.removeAllItems();
        cboLocTheLoai.removeAllItems();
        cboLocTheLoai.addItem(new TheLoaiDTO(0, "-- Tất cả thể loại --", ""));
        for (TheLoaiDTO tl : sachBUS.getAllTheLoai()) {
            cboTheLoai.addItem(tl);
            cboLocTheLoai.addItem(tl);
        }
    }

    private void selectTheLoaiByName(String tenTL) {
        for (int i = 0; i < cboTheLoai.getItemCount(); i++) {
            if (cboTheLoai.getItemAt(i).getTenTheLoai().equals(tenTL)) {
                cboTheLoai.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectTinhTrang(String tinhTrang) {
        if (tinhTrang == null || tinhTrang.trim().isEmpty()) {
            cboTinhTrang.setSelectedItem("Bình thường");
            return;
        }
        for (int i = 0; i < cboTinhTrang.getItemCount(); i++) {
            if (cboTinhTrang.getItemAt(i).equalsIgnoreCase(tinhTrang.trim())) {
                cboTinhTrang.setSelectedIndex(i);
                return;
            }
        }
        cboTinhTrang.setSelectedItem("Bình thường");
    }

    private String val(int row, int col) {
        Object o = tableModel.getValueAt(row, col);
        return o != null ? o.toString() : "";
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadData(List<SachDTO> list) {
        this.currentList = list;
        tableModel.setRowCount(0);
        for (SachDTO s : list) {
            tableModel.addRow(new Object[]{
                    s.getMaSach(),
                    s.getTenSach(),
                    s.getTenTheLoai(),
                    s.getTacGia(),
                    s.getNhaXuatBan(),
                    s.getNamXuatBan(),
                    s.getIsbn(),
                    s.getSoLuongTong(),
                    s.getSoLuongConLai(),
                    s.getSoLuongThanhLy(),
                    s.getTinhTrangSach(),
                    s.getViTriKe(),
                    s.isTrangThai() ? "Lưu hành" : "Đã ẩn"
            });
        }
    }

    private void xuLyThemTheLoai() {
        String ten = JOptionPane.showInputDialog(this, "Nhập tên thể loại mới:", "Thêm thể loại", JOptionPane.PLAIN_MESSAGE);
        if (ten != null) {
            String msg = sachBUS.addTheLoai(ten, "");
            if ("OK".equals(msg)) {
                JOptionPane.showMessageDialog(this, "Thêm thể loại thành công!");
                loadTheLoai();
            } else {
                JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private SachDTO buildSachFromForm(int maSach) {
        TheLoaiDTO tl = (TheLoaiDTO) cboTheLoai.getSelectedItem();
        String tinhTrang = cboTinhTrang.getSelectedItem() != null
                ? cboTinhTrang.getSelectedItem().toString()
                : "Bình thường";
        int soLuongThanhLy = 0;
        if (maSach > 0 && currentList != null) {
            for (SachDTO item : currentList) {
                if (item.getMaSach() == maSach) {
                    soLuongThanhLy = item.getSoLuongThanhLy();
                    break;
                }
            }
        }
        return new SachDTO(
                maSach,
                tl != null ? tl.getMaTheLoai() : 1,
                tl != null ? tl.getTenTheLoai() : "",
                txtTenSach.getText().trim(),
                txtTacGia.getText().trim(),
                txtNXB.getText().trim(),
                Integer.parseInt(txtNamXB.getText().trim()),
                txtISBN.getText().trim(),
                Integer.parseInt(txtSoLuongTong.getText().trim()),
                Integer.parseInt(txtSoLuongCon.getText().trim()),
                txtViTriKe.getText().trim(),
                chkLuuHanh.isSelected(),
                tinhTrang,
                soLuongThanhLy
        );
    }

    private void xuLyThemSach() {
        try {
            SachDTO s = buildSachFromForm(0);
            String msg = sachBUS.addSach(s);
            if ("OK".equals(msg)) {
                JOptionPane.showMessageDialog(this, "Thêm sách mới thành công!");
                loadData(sachBUS.getAllSach());
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Năm xuất bản và Số lượng phải là số nguyên!", "Sai định dạng", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void xuLySuaSach() {
        if (txtMaSach.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một cuốn sách trong bảng để cập nhật!");
            return;
        }
        try {
            SachDTO s = buildSachFromForm(Integer.parseInt(txtMaSach.getText()));
            String msg = sachBUS.updateSach(s);
            if ("OK".equals(msg)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thông tin sách thành công!");
                loadData(sachBUS.getAllSach());
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Năm xuất bản và Số lượng phải là số nguyên!", "Sai định dạng", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void xuLyThanhLySach() {
        if (txtMaSach.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đầu sách trong bảng để thanh lý!");
            return;
        }
        int maSach = Integer.parseInt(txtMaSach.getText());
        String tenSach = txtTenSach.getText().trim();
        int conLaiHienTai = 0;
        try {
            conLaiHienTai = Integer.parseInt(txtSoLuongCon.getText().trim());
        } catch (NumberFormatException ignored) {
        }

        if (conLaiHienTai <= 0) {
            JOptionPane.showMessageDialog(this, "Đầu sách này không còn số lượng tồn kho để thanh lý!", "Không thể thanh lý", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String input = JOptionPane.showInputDialog(
                this,
                "Đầu sách: " + tenSach + "\nSố lượng đang còn trong kho: " + conLaiHienTai + "\nNhập số lượng cần thanh lý:",
                "Thanh lý sách",
                JOptionPane.PLAIN_MESSAGE
        );
        if (input == null) return;

        try {
            int slThanhLy = Integer.parseInt(input.trim());
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận thanh lý " + slThanhLy + " cuốn của đầu sách [" + tenSach + "]?",
                    "Xác nhận thanh lý",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                String msg = sachBUS.thanhLySach(maSach, slThanhLy, "Thanh lý");
                if ("OK".equals(msg)) {
                    JOptionPane.showMessageDialog(this, "Đã ghi nhận thanh lý " + slThanhLy + " cuốn!");
                    loadData(sachBUS.getAllSach());
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, msg, "Không thể thanh lý", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng thanh lý phải là số nguyên!", "Sai định dạng", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void xuLyAnHien() {
        if (txtMaSach.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách trong bảng!");
            return;
        }
        int maSach = Integer.parseInt(txtMaSach.getText());
        boolean trangThaiMoi = !chkLuuHanh.isSelected();
        String actionText = trangThaiMoi ? "HIỆN LẠI" : "ẨN (XÓA MỀM)";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn " + actionText + " cuốn sách này?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && sachBUS.toggleTrangThai(maSach, trangThaiMoi)) {
            JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái sách!");
            loadData(sachBUS.getAllSach());
            clearForm();
        }
    }

    private void xuLyTimKiem() {
        TheLoaiDTO selected = (TheLoaiDTO) cboLocTheLoai.getSelectedItem();
        int maTL = selected != null ? selected.getMaTheLoai() : 0;
        loadData(sachBUS.searchSach(txtTimKiem.getText(), maTL));
    }

    private void clearForm() {
        txtMaSach.setText("");
        txtTenSach.setText("");
        if (cboTheLoai.getItemCount() > 0) cboTheLoai.setSelectedIndex(0);
        txtTacGia.setText("");
        txtNXB.setText("");
        txtNamXB.setText("2024");
        txtISBN.setText("");
        txtSoLuongTong.setText("1");
        txtSoLuongCon.setText("1");
        cboTinhTrang.setSelectedItem("Bình thường");
        txtViTriKe.setText("");
        chkLuuHanh.setSelected(true);
        table.clearSelection();
    }
}