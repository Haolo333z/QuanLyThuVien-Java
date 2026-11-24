package gui;

import com.formdev.flatlaf.FlatLightLaf;
import utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;

    private JButton btnThongKe;
    private JButton btnSach;
    private JButton btnDocGia;
    private JButton btnMuonTra;
    private JButton btnNhanVien;
    private JButton btnDangXuat;

    private String hoTen;
    private String vaiTro; // "Admin" hoặc "ThuThu"

    public MainFrame(String hoTen, String vaiTro) {
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;

        initUI();
        applyPermissions();
    }

    private void initUI() {
        setTitle("Hệ Thống Quản Lý Thư Viện");
        setSize(1200, 720);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. SIDEBAR BÊN TRÁI
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(new Color(30, 41, 59));
        sidebar.setLayout(new BorderLayout());

        // Header Sidebar
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(25, 20, 20, 20));

        JLabel lblAppTitle = new JLabel("QUẢN LÝ THƯ VIỆN");
        lblAppTitle.setForeground(Color.WHITE);
        lblAppTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAppTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblUser = new JLabel("Xin chào: " + hoTen);
        lblUser.setForeground(new Color(203, 213, 225));
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setBorder(new EmptyBorder(10, 0, 2, 0));
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblRole = new JLabel("Vai trò: " + vaiTro);
        lblRole.setForeground(new Color(56, 189, 248));
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(lblAppTitle);
        headerPanel.add(lblUser);
        headerPanel.add(lblRole);

        // Danh sách nút menu
        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new GridLayout(6, 1, 0, 8));
        menuPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        btnThongKe = createMenuButton("Thống kê & Tổng quan");
        btnSach = createMenuButton("Quản lý Sách");
        btnDocGia = createMenuButton("Quản lý Độc giả");
        btnMuonTra = createMenuButton("Mượn / Trả sách");
        btnNhanVien = createMenuButton("Quản lý Nhân viên");

        menuPanel.add(btnThongKe);
        menuPanel.add(btnSach);
        menuPanel.add(btnDocGia);
        menuPanel.add(btnMuonTra);
        menuPanel.add(btnNhanVien);

        // Nút Đăng xuất
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(15, 15, 20, 15));

        btnDangXuat = new JButton("Đăng xuất");
        btnDangXuat.setFocusPainted(false);
        btnDangXuat.setBackground(new Color(220, 38, 38));
        btnDangXuat.setForeground(Color.WHITE);
        btnDangXuat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDangXuat.setPreferredSize(new Dimension(0, 40));
        btnDangXuat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        footerPanel.add(btnDangXuat, BorderLayout.CENTER);

        sidebar.add(headerPanel, BorderLayout.NORTH);
        sidebar.add(menuPanel, BorderLayout.CENTER);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        // 2. VÙNG NỘI DUNG CHÍNH (CARDLAYOUT)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(248, 250, 252));

        contentPanel.add(new ThongKePanel(), "THONG_KE");
        contentPanel.add(new SachPanel(), "SACH");
        contentPanel.add(new DocGiaPanel(), "DOC_GIA");
        contentPanel.add(new MuonTraPanel(), "MUON_TRA");
        contentPanel.add(new NhanVienPanel(), "NHAN_VIEN");

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // 3. SỰ KIỆN CHUYỂN TAB & ĐĂNG XUẤT
        btnThongKe.addActionListener(e -> cardLayout.show(contentPanel, "THONG_KE"));
        btnSach.addActionListener(e -> cardLayout.show(contentPanel, "SACH"));
        btnDocGia.addActionListener(e -> cardLayout.show(contentPanel, "DOC_GIA"));
        btnMuonTra.addActionListener(e -> cardLayout.show(contentPanel, "MUON_TRA"));
        btnNhanVien.addActionListener(e -> cardLayout.show(contentPanel, "NHAN_VIEN"));

        btnDangXuat.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc chắn muốn đăng xuất?",
                    "Xác nhận đăng xuất",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                Session.clear();
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(51, 65, 85));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 250, 252));
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(new Color(71, 85, 105));
        panel.add(label);
        return panel;
    }

    private void applyPermissions() {
        if (!"Admin".equalsIgnoreCase(vaiTro)) {
            btnNhanVien.setVisible(false);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new MainFrame("Lê Hữu Hào", "Admin").setVisible(true));
    }
}