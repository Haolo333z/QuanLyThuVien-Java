package gui;

import bus.DocGiaBUS;
import dto.DocGiaDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class DocGiaPanel extends JPanel {
    private DocGiaBUS docGiaBUS = new DocGiaBUS();

    private JTextField txtMaDG, txtHoTen, txtNgaySinh, txtSDT, txtEmail, txtDiaChi, txtNgayLap, txtNgayHetHan, txtTimKiem;
    private JComboBox<String> cboGioiTinh;
    private JCheckBox chkHoatDong;
    private JTable table;
    private DefaultTableModel tableModel;

    public DocGiaPanel() {
        initUI();
        loadData(docGiaBUS.getAll());
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 250, 252));

        // 1. HEADER & TÌM KIẾM
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ ĐỘC GIẢ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        txtTimKiem = new JTextField(20);
        txtTimKiem.putClientProperty("JTextField.placeholderText", "Tìm theo tên, SĐT, email...");
        JButton btnTim = new JButton("Tìm kiếm");
        JButton btnTaiLai = new JButton("Tải lại");
        searchPanel.add(txtTimKiem);
        searchPanel.add(btnTim);
        searchPanel.add(btnTaiLai);

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        // 2. FORM NHẬP THÔNG TIN
        JPanel formContainer = new JPanel(new BorderLayout(10, 10));
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("Thông tin thẻ độc giả"),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JPanel fieldsPanel = new JPanel(new GridLayout(5, 4, 12, 8));
        fieldsPanel.setBackground(Color.WHITE);

        txtMaDG = new JTextField(); txtMaDG.setEditable(false);
        txtHoTen = new JTextField();
        txtNgaySinh = new JTextField();
        txtNgaySinh.putClientProperty("JTextField.placeholderText", "yyyy-MM-dd (VD: 2005-02-15)");
        cboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        txtSDT = new JTextField();
        txtEmail = new JTextField();
        txtDiaChi = new JTextField();
        txtNgayLap = new JTextField(); txtNgayLap.setEditable(false);
        txtNgayHetHan = new JTextField(); txtNgayHetHan.setEditable(false);
        chkHoatDong = new JCheckBox("Đang hoạt động", true);
        chkHoatDong.setBackground(Color.WHITE);

        fieldsPanel.add(new JLabel("Mã độc giả:"));     fieldsPanel.add(txtMaDG);
        fieldsPanel.add(new JLabel("Họ tên (*):"));     fieldsPanel.add(txtHoTen);
        fieldsPanel.add(new JLabel("Ngày sinh:"));      fieldsPanel.add(txtNgaySinh);
        fieldsPanel.add(new JLabel("Giới tính:"));      fieldsPanel.add(cboGioiTinh);
        fieldsPanel.add(new JLabel("Số ĐT (*):"));      fieldsPanel.add(txtSDT);
        fieldsPanel.add(new JLabel("Email:"));          fieldsPanel.add(txtEmail);
        fieldsPanel.add(new JLabel("Địa chỉ:"));        fieldsPanel.add(txtDiaChi);
        fieldsPanel.add(new JLabel("Trạng thái:"));     fieldsPanel.add(chkHoatDong);
        fieldsPanel.add(new JLabel("Ngày lập thẻ:"));   fieldsPanel.add(txtNgayLap);
        fieldsPanel.add(new JLabel("Ngày hết hạn:"));   fieldsPanel.add(txtNgayHetHan);

        // Nút thao tác
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnThem = createActionButton("Lập thẻ mới (+1 năm)", new Color(22, 163, 74));
        JButton btnSua = createActionButton("Cập nhật", new Color(37, 99, 235));
        JButton btnGiaHan = createActionButton("Gia hạn (+1 năm)", new Color(147, 51, 234));
        JButton btnKhoa = createActionButton("Khóa / Mở khóa", new Color(234, 88, 12));
        JButton btnLamMoi = createActionButton("Làm mới", new Color(100, 116, 139));

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnGiaHan);
        buttonPanel.add(btnKhoa);
        buttonPanel.add(btnLamMoi);

        formContainer.add(fieldsPanel, BorderLayout.CENTER);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        // 3. BẢNG DANH SÁCH
        String[] columns = {"Mã ĐG", "Họ tên", "Ngày sinh", "Giới tính", "SĐT", "Email", "Địa chỉ", "Ngày lập", "Ngày hết hạn", "Trạng thái"};
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
                txtMaDG.setText(val(r, 0));
                txtHoTen.setText(val(r, 1));
                txtNgaySinh.setText(val(r, 2));
                String gioiTinh = val(r, 3);
                if (!gioiTinh.isEmpty()) {
                    cboGioiTinh.setSelectedItem(gioiTinh);
                } else {
                    cboGioiTinh.setSelectedIndex(0);
                }
                txtSDT.setText(val(r, 4));
                txtEmail.setText(val(r, 5));
                txtDiaChi.setText(val(r, 6));
                txtNgayLap.setText(val(r, 7));
                txtNgayHetHan.setText(val(r, 8));
                chkHoatDong.setSelected("Hoạt động".equals(val(r, 9)));
            }
        });

        btnThem.addActionListener(e -> xuLyThem());
        btnSua.addActionListener(e -> xuLySua());
        btnGiaHan.addActionListener(e -> xuLyGiaHan());
        btnKhoa.addActionListener(e -> xuLyKhoaMo());
        btnLamMoi.addActionListener(e -> clearForm());
        btnTim.addActionListener(e -> loadData(docGiaBUS.search(txtTimKiem.getText())));
        btnTaiLai.addActionListener(e -> {
            txtTimKiem.setText("");
            loadData(docGiaBUS.getAll());
        });
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

    private void loadData(List<DocGiaDTO> list) {
        tableModel.setRowCount(0);
        for (DocGiaDTO dg : list) {
            tableModel.addRow(new Object[]{
                    dg.getMaDocGia(),
                    dg.getHoTen(),
                    dg.getNgaySinh() != null ? dg.getNgaySinh().toString() : "",
                    dg.getGioiTinh(),
                    dg.getSoDienThoai(),
                    dg.getEmail(),
                    dg.getDiaChi(),
                    dg.getNgayLapThe().toString(),
                    dg.getNgayHetHan().toString(),
                    dg.isTrangThai() ? "Hoạt động" : "Đã khóa"
            });
        }
    }

    public void refreshData() {
        txtTimKiem.setText("");
        loadData(docGiaBUS.getAll());
    }

    private LocalDate parseNgaySinh() {
        String s = txtNgaySinh.getText().trim();
        if (s.isEmpty()) return null;
        return LocalDate.parse(s); // Định dạng yyyy-MM-dd
    }

    private void xuLyThem() {
        try {
            DocGiaDTO dg = new DocGiaDTO(
                    0,
                    txtHoTen.getText().trim(),
                    parseNgaySinh(),
                    cboGioiTinh.getSelectedItem().toString(),
                    txtSDT.getText().trim(),
                    txtEmail.getText().trim(),
                    txtDiaChi.getText().trim(),
                    null, null,
                    chkHoatDong.isSelected()
            );
            String msg = docGiaBUS.addDocGia(dg);
            if ("OK".equals(msg)) {
                JOptionPane.showMessageDialog(this, "Lập thẻ độc giả mới thành công (Hạn dùng +1 năm)!");
                loadData(docGiaBUS.getAll());
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày sinh phải đúng định dạng yyyy-MM-dd (VD: 2005-02-15)!",
                    "Sai định dạng ngày", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLySua() {
        if (txtMaDG.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn độc giả trong bảng để cập nhật!");
            return;
        }
        try {
            DocGiaDTO dg = new DocGiaDTO(
                    Integer.parseInt(txtMaDG.getText()),
                    txtHoTen.getText().trim(),
                    parseNgaySinh(),
                    cboGioiTinh.getSelectedItem().toString(),
                    txtSDT.getText().trim(),
                    txtEmail.getText().trim(),
                    txtDiaChi.getText().trim(),
                    LocalDate.parse(txtNgayLap.getText()),
                    LocalDate.parse(txtNgayHetHan.getText()),
                    chkHoatDong.isSelected()
            );
            String msg = docGiaBUS.updateDocGia(dg);
            if ("OK".equals(msg)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thông tin độc giả thành công!");
                loadData(docGiaBUS.getAll());
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày sinh phải đúng định dạng yyyy-MM-dd!",
                    "Sai định dạng ngày", JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mã độc giả không hợp lệ!",
                    "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyGiaHan() {
        if (txtMaDG.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn độc giả cần gia hạn thẻ!");
            return;
        }
        try {
            int maDG = Integer.parseInt(txtMaDG.getText());
            LocalDate hanCu = txtNgayHetHan.getText().trim().isEmpty()
                    ? null
                    : LocalDate.parse(txtNgayHetHan.getText().trim());
            String res = docGiaBUS.giaHanThe(maDG, hanCu);
            if (res.startsWith("OK:")) {
                JOptionPane.showMessageDialog(this, "Gia hạn thành công! Ngày hết hạn mới: " + res.substring(3));
                loadData(docGiaBUS.getAll());
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, res, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DateTimeParseException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Dữ liệu thẻ độc giả không hợp lệ để gia hạn!",
                    "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void xuLyKhoaMo() {
        if (txtMaDG.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn độc giả trong bảng!");
            return;
        }
        int maDG = Integer.parseInt(txtMaDG.getText());
        boolean trangThaiMoi = !chkHoatDong.isSelected();
        String actionText = trangThaiMoi ? "MỞ KHÓA" : "KHÓA";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn " + actionText + " thẻ độc giả này?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && docGiaBUS.toggleTrangThai(maDG, trangThaiMoi)) {
            JOptionPane.showMessageDialog(this, "Đã " + actionText.toLowerCase() + " thẻ độc giả!");
            loadData(docGiaBUS.getAll());
            clearForm();
        }
    }

    private void clearForm() {
        txtMaDG.setText("");
        txtHoTen.setText("");
        txtNgaySinh.setText("");
        cboGioiTinh.setSelectedIndex(0);
        txtSDT.setText("");
        txtEmail.setText("");
        txtDiaChi.setText("");
        txtNgayLap.setText("");
        txtNgayHetHan.setText("");
        chkHoatDong.setSelected(true);
        table.clearSelection();
    }
}