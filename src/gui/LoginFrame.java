package gui;

import bus.NhanVienBUS;
import com.formdev.flatlaf.FlatLightLaf;
import dto.NhanVienDTO;
import utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private NhanVienBUS nhanVienBUS = new NhanVienBUS();

    public LoginFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Đăng Nhập - Quản Lý Thư Viện");
        setSize(420, 320);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBorder(new EmptyBorder(25, 35, 25, 35));
        mainPanel.setBackground(Color.WHITE);

        // Tiêu đề
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 41, 59));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Form nhập liệu
        JPanel formPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        formPanel.setBackground(Color.WHITE);

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        formPanel.add(lblUser);
        formPanel.add(txtUsername);
        formPanel.add(lblPass);
        formPanel.add(txtPassword);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Nút đăng nhập
        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setBackground(new Color(37, 99, 235));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(0, 40));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mainPanel.add(btnLogin, BorderLayout.SOUTH);

        add(mainPanel);

        // Sự kiện bấm nút hoặc nhấn Enter
        btnLogin.addActionListener(e -> xuLyDangNhap());
        getRootPane().setDefaultButton(btnLogin);
    }

    private void xuLyDangNhap() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        NhanVienDTO nv = nhanVienBUS.dangNhap(user, pass);
        if (nv != null) {
            Session.setCurrentUser(nv);
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công! Xin chào " + nv.getHoTen());
            dispose(); // Đóng form Login
            new MainFrame(nv).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Sai tài khoản, mật khẩu hoặc tài khoản đã bị khóa!",
                    "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}