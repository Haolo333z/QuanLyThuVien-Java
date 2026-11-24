package gui;

import bus.NhanVienBUS;
import dto.NhanVienDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class NhanVienPanel extends JPanel {
    private NhanVienBUS nhanVienBUS = new NhanVienBUS();

    private JTextField txtMaNV, txtHoTen, txtTenDangNhap, txtMatKhau, txtSDT, txtEmail, txtTimKiem;
    private JComboBox<String> cboVaiTro;
    private JCheckBox chkHoatDong;
    private JTable table;
    private DefaultTableModel tableModel;

    public NhanVienPanel() {
        initUI();
        loadData(nhanVienBUS.getAll());
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 250, 252));

        // 1. HEADER & THANH TÌM KIẾM
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN VIÊN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        txtTimKiem = new JTextField(20);
        txtTimKiem.putClientProperty("JTextField.placeholderText", "Tìm theo tên, tài khoản, SĐT...");
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
                new TitledBorder("Thông tin nhân viên"),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JPanel fieldsPanel = new JPanel(new GridLayout(4, 4, 12, 10));
        fieldsPanel.setBackground(Color.WHITE);

        txtMaNV = new JTextField();
        txtMaNV.setEditable(false);
        txtHoTen = new JTextField();
        txtTenDangNhap = new JTextField();
        txtMatKhau = new JTextField();
        cboVaiTro = new JComboBox<>(new String[]{"ThuThu", "Admin"});
        txtSDT = new JTextField();
        txtEmail = new JTextField();
        chkHoatDong = new JCheckBox("Đang hoạt động", true);
        chkHoatDong.setBackground(Color.WHITE);

        fieldsPanel.add(new JLabel("Mã NV:"));          fieldsPanel.add(txtMaNV);
        fieldsPanel.add(new JLabel("Họ tên:"));         fieldsPanel.add(txtHoTen);
        fieldsPanel.add(new JLabel("Tên đăng nhập:"));  fieldsPanel.add(txtTenDangNhap);
        fieldsPanel.add(new JLabel("Mật khẩu:"));       fieldsPanel.add(txtMatKhau);
        fieldsPanel.add(new JLabel("Vai trò:"));        fieldsPanel.add(cboVaiTro);
        fieldsPanel.add(new JLabel("Số điện thoại:"));  fieldsPanel.add(txtSDT);
        fieldsPanel.add(new JLabel("Email:"));          fieldsPanel.add(txtEmail);
        fieldsPanel.add(new JLabel("Trạng thái:"));     fieldsPanel.add(chkHoatDong);

        // Các nút thao tác CRUD
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnThem = createActionButton("Thêm mới", new Color(22, 163, 74));
        JButton btnSua = createActionButton("Cập nhật", new Color(37, 99, 235));
        JButton btnKhoa = createActionButton("Khóa / Mở khóa", new Color(234, 88, 12));
        JButton btnLamMoi = createActionButton("Làm mới form", new Color(100, 116, 139));

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnKhoa);
        buttonPanel.add(btnLamMoi);

        formContainer.add(fieldsPanel, BorderLayout.CENTER);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        // 3. BẢNG DANH SÁCH NHÂN VIÊN
        String[] columns = {"Mã NV", "Họ tên", "Tên đăng nhập", "Mật khẩu", "Vai trò", "SĐT", "Email", "Trạng thái"};
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

        // Ghép layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(formContainer, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // 4. GẮN SỰ KIỆN
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                txtMaNV.setText(tableModel.getValueAt(row, 0).toString());
                txtHoTen.setText(tableModel.getValueAt(row, 1).toString());
                txtTenDangNhap.setText(tableModel.getValueAt(row, 2).toString());
                txtTenDangNhap.setEditable(false); // Không cho đổi username khi sửa
                txtMatKhau.setText(tableModel.getValueAt(row, 3).toString());
                cboVaiTro.setSelectedItem(tableModel.getValueAt(row, 4).toString());
                txtSDT.setText(tableModel.getValueAt(row, 5) != null ? tableModel.getValueAt(row, 5).toString() : "");
                txtEmail.setText(tableModel.getValueAt(row, 6) != null ? tableModel.getValueAt(row, 6).toString() : "");
                chkHoatDong.setSelected("Hoạt động".equals(tableModel.getValueAt(row, 7).toString()));
            }
        });

        btnThem.addActionListener(e -> xuLyThem());
        btnSua.addActionListener(e -> xuLySua());
        btnKhoa.addActionListener(e -> xuLyKhoaMo());
        btnLamMoi.addActionListener(e -> clearForm());
        btnTim.addActionListener(e -> loadData(nhanVienBUS.search(txtTimKiem.getText())));
        btnTaiLai.addActionListener(e -> {
            txtTimKiem.setText("");
            loadData(nhanVienBUS.getAll());
        });
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

    private void loadData(List<NhanVienDTO> list) {
        tableModel.setRowCount(0);
        for (NhanVienDTO nv : list) {
            tableModel.addRow(new Object[]{
                    nv.getMaNV(),
                    nv.getHoTen(),
                    nv.getTenDangNhap(),
                    nv.getMatKhauHash(),
                    nv.getVaiTro(),
                    nv.getSoDienThoai(),
                    nv.getEmail(),
                    nv.isTrangThai() ? "Hoạt động" : "Đã khóa"
            });
        }
    }

    private void xuLyThem() {
        NhanVienDTO nv = new NhanVienDTO(
                0,
                txtHoTen.getText().trim(),
                txtTenDangNhap.getText().trim(),
                txtMatKhau.getText().trim(),
                cboVaiTro.getSelectedItem().toString(),
                txtSDT.getText().trim(),
                txtEmail.getText().trim(),
                chkHoatDong.isSelected()
        );
        String msg = nhanVienBUS.addNhanVien(nv);
        if ("OK".equals(msg)) {
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
            loadData(nhanVienBUS.getAll());
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void xuLySua() {
        if (txtMaNV.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên trong bảng để cập nhật!");
            return;
        }
        NhanVienDTO nv = new NhanVienDTO(
                Integer.parseInt(txtMaNV.getText()),
                txtHoTen.getText().trim(),
                txtTenDangNhap.getText().trim(),
                txtMatKhau.getText().trim(),
                cboVaiTro.getSelectedItem().toString(),
                txtSDT.getText().trim(),
                txtEmail.getText().trim(),
                chkHoatDong.isSelected()
        );
        String msg = nhanVienBUS.updateNhanVien(nv);
        if ("OK".equals(msg)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!");
            loadData(nhanVienBUS.getAll());
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void xuLyKhoaMo() {
        if (txtMaNV.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên trong bảng!");
            return;
        }
        int maNV = Integer.parseInt(txtMaNV.getText());
        boolean trangThaiMoi = !chkHoatDong.isSelected();
        String actionText = trangThaiMoi ? "MỞ KHÓA" : "KHÓA";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn " + actionText + " tài khoản này?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (nhanVienBUS.toggleTrangThai(maNV, trangThaiMoi)) {
                JOptionPane.showMessageDialog(this, "Đã " + actionText.toLowerCase() + " tài khoản thành công!");
                loadData(nhanVienBUS.getAll());
                clearForm();
            }
        }
    }

    private void clearForm() {
        txtMaNV.setText("");
        txtHoTen.setText("");
        txtTenDangNhap.setText("");
        txtTenDangNhap.setEditable(true);
        txtMatKhau.setText("");
        cboVaiTro.setSelectedIndex(0);
        txtSDT.setText("");
        txtEmail.setText("");
        chkHoatDong.setSelected(true);
        table.clearSelection();
    }
}