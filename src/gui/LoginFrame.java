package gui;

import bus.NhanVienBUS;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import dto.NhanVienDTO;
import utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class LoginFrame extends JFrame {
    private static final String PREF_REMEMBER_USER = "remembered_username";
    private static final File PREF_FILE = new File(
            System.getProperty("user.home"), ".quanlythuvien-login.properties"
    );
    private static final Color BRAND_DARK_TOP = new Color(30, 41, 59);
    private static final Color BRAND_DARK_BOTTOM = new Color(15, 23, 42);
    private static final Color PRIMARY_BLUE = new Color(37, 99, 235);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color ERROR_COLOR = new Color(220, 38, 38);
    private static final Color ERROR_BG = new Color(254, 242, 242);
    private static final Color WARNING_COLOR = new Color(217, 119, 6);

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkRemember;
    private JButton btnLogin;
    private JLabel lblError;
    private JLabel lblCapsLock;

    private final NhanVienBUS nhanVienBUS = new NhanVienBUS();

    public LoginFrame() {
        initUI();
        loadSavedUsername();
        bindEvents();
    }

    private void initUI() {
        setTitle("Đăng Nhập - Hệ Thống Quản Lý Thư Viện");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel rootPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        rootPanel.setPreferredSize(new Dimension(880, 540));
        rootPanel.add(createBrandPanel());
        rootPanel.add(createFormPanel());
        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createBrandPanel() {
        JPanel brandPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, BRAND_DARK_TOP,
                        getWidth(), getHeight(), BRAND_DARK_BOTTOM
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Vòng tròn trang trí mờ tạo chiều sâu hiện đại
                g2.setColor(new Color(56, 189, 248, 22));
                g2.fill(new Ellipse2D.Double(-70, -60, 240, 240));

                g2.setColor(new Color(37, 99, 235, 32));
                g2.fill(new Ellipse2D.Double(getWidth() - 150, getHeight() - 170, 260, 260));

                g2.setColor(new Color(255, 255, 255, 14));
                g2.drawOval(getWidth() - 110, 40, 160, 160);
                g2.dispose();
            }
        };
        brandPanel.setBorder(new EmptyBorder(40, 38, 32, 38));

        // Logo & Tiêu đề thương hiệu
        JPanel topBrand = new JPanel();
        topBrand.setOpaque(false);
        topBrand.setLayout(new BoxLayout(topBrand, BoxLayout.Y_AXIS));

        JLabel lblLogo = new JLabel(createLibraryBadgeIcon());
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblBrandName = new JLabel("QUẢN LÝ THƯ VIỆN");
        lblBrandName.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblBrandName.setForeground(Color.WHITE);
        lblBrandName.setBorder(new EmptyBorder(18, 0, 6, 0));
        lblBrandName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTagline = new JLabel(
                "<html><body style='width:255px'>Nền tảng quản trị đầu sách, độc giả và quy trình mượn trả tập trung, chính xác.</body></html>"
        );
        lblTagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTagline.setForeground(new Color(148, 163, 184));
        lblTagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        topBrand.add(lblLogo);
        topBrand.add(lblBrandName);
        topBrand.add(lblTagline);

        // Danh sách điểm nổi bật
        JPanel featuresPanel = new JPanel();
        featuresPanel.setOpaque(false);
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setBorder(new EmptyBorder(24, 0, 24, 0));

        featuresPanel.add(createFeatureRow("Tra cứu & cập nhật kho sách theo thời gian thực"));
        featuresPanel.add(Box.createVerticalStrut(12));
        featuresPanel.add(createFeatureRow("Theo dõi phiếu mượn, trả sách và cảnh báo quá hạn"));
        featuresPanel.add(Box.createVerticalStrut(12));
        featuresPanel.add(createFeatureRow("Phân quyền bảo mật cho Quản trị viên & Thủ thư"));

        JLabel lblFooter = new JLabel("Phiên bản 1.0  •  Library Management System");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooter.setForeground(new Color(100, 116, 139));

        brandPanel.add(topBrand, BorderLayout.NORTH);
        brandPanel.add(featuresPanel, BorderLayout.CENTER);
        brandPanel.add(lblFooter, BorderLayout.SOUTH);
        return brandPanel;
    }

    private JPanel createFeatureRow(String text) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dot = new JLabel(createCheckBadgeIcon());
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(226, 232, 240));

        row.add(dot, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        return row;
    }

    private JPanel createFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(24, 44, 24, 44));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setOpaque(false);

        JLabel lblWelcome = new JLabel("Chào mừng trở lại");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcome.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Đăng nhập tài khoản nhân viên để tiếp tục");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_SECONDARY);

        // Khung thông báo lỗi nội tuyến
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(ERROR_COLOR);
        lblError.setBackground(ERROR_BG);
        lblError.setOpaque(true);
        lblError.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(254, 202, 202), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        lblError.setVisible(false);

        // Tên đăng nhập
        JLabel lblUser = createFieldLabel("Tên đăng nhập");
        txtUsername = new JTextField();
        styleInputField(txtUsername, "Nhập tên đăng nhập...", createUserIcon());
        txtUsername.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);

        // Mật khẩu
        JLabel lblPass = createFieldLabel("Mật khẩu");
        txtPassword = new JPasswordField();
        styleInputField(txtPassword, "Nhập mật khẩu...", createLockIcon());
        txtPassword.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; showRevealButton: true; focusWidth: 2; innerFocusWidth: 0");

        // Ghi nhớ + cảnh báo Caps Lock
        JPanel optionsRow = new JPanel(new BorderLayout());
        optionsRow.setOpaque(false);
        optionsRow.setPreferredSize(new Dimension(340, 26));

        chkRemember = new JCheckBox("Ghi nhớ tên đăng nhập");
        chkRemember.setOpaque(false);
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkRemember.setForeground(new Color(71, 85, 105));
        chkRemember.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkRemember.setFocusPainted(false);

        lblCapsLock = new JLabel("Caps Lock đang bật");
        lblCapsLock.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblCapsLock.setForeground(WARNING_COLOR);
        lblCapsLock.setVisible(false);

        optionsRow.add(chkRemember, BorderLayout.WEST);
        optionsRow.add(lblCapsLock, BorderLayout.EAST);

        // Nút Đăng nhập
        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(PRIMARY_BLUE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(340, 44));
        btnLogin.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; borderWidth: 0; focusWidth: 0; hoverBackground: #1D4ED8; pressedBackground: #1E40AF");

        JLabel lblHint = new JLabel("Gặp sự cố đăng nhập? Vui lòng liên hệ Quản trị viên.");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(new Color(148, 163, 184));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0);
        formCard.add(lblWelcome, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 18, 0);
        formCard.add(lblSub, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 12, 0);
        formCard.add(lblError, gbc);

        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 6, 0);
        formCard.add(lblUser, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 14, 0);
        formCard.add(txtUsername, gbc);

        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 6, 0);
        formCard.add(lblPass, gbc);

        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 10, 0);
        formCard.add(txtPassword, gbc);

        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 18, 0);
        formCard.add(optionsRow, gbc);

        gbc.gridy = 8; gbc.insets = new Insets(0, 0, 16, 0);
        formCard.add(btnLogin, gbc);

        gbc.gridy = 9; gbc.insets = new Insets(0, 0, 0, 0);
        formCard.add(lblHint, gbc);

        wrapper.add(formCard);
        return wrapper;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(51, 65, 85));
        return label;
    }

    private void styleInputField(JTextField field, String placeholder, Icon leadingIcon) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(340, 42));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, leadingIcon);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10; focusWidth: 2; innerFocusWidth: 0");
    }

    private void bindEvents() {
        btnLogin.addActionListener(e -> xuLyDangNhap());
        getRootPane().setDefaultButton(btnLogin);

        txtUsername.addActionListener(e -> {
            if (txtPassword.getPassword().length == 0) {
                txtPassword.requestFocusInWindow();
            } else {
                xuLyDangNhap();
            }
        });

        DocumentListener clearErrorListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { clearErrorState(); }
            @Override
            public void removeUpdate(DocumentEvent e) { clearErrorState(); }
            @Override
            public void changedUpdate(DocumentEvent e) { clearErrorState(); }
        };
        txtUsername.getDocument().addDocumentListener(clearErrorListener);
        txtPassword.getDocument().addDocumentListener(clearErrorListener);

        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateCapsLockIndicator();
            }
        });
        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCapsLockIndicator();
            }

            @Override
            public void focusLost(FocusEvent e) {
                lblCapsLock.setVisible(false);
            }
        });
    }

    private void loadSavedUsername() {
        String saved = "";
        if (PREF_FILE.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(PREF_FILE)) {
                props.load(fis);
                saved = props.getProperty(PREF_REMEMBER_USER, "");
            } catch (Exception ignored) {
            }
        }
        if (!saved.isEmpty()) {
            txtUsername.setText(saved);
            chkRemember.setSelected(true);
            SwingUtilities.invokeLater(() -> txtPassword.requestFocusInWindow());
        } else {
            SwingUtilities.invokeLater(() -> txtUsername.requestFocusInWindow());
        }
    }

    private void saveRememberedUsername(String usernameOrEmpty) {
        Properties props = new Properties();
        if (usernameOrEmpty != null && !usernameOrEmpty.isEmpty()) {
            props.setProperty(PREF_REMEMBER_USER, usernameOrEmpty);
        }
        try (FileOutputStream fos = new FileOutputStream(PREF_FILE)) {
            props.store(fos, "Login preferences");
        } catch (Exception ignored) {
        }
    }

    private void updateCapsLockIndicator() {
        try {
            boolean capsOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
            lblCapsLock.setVisible(capsOn);
        } catch (UnsupportedOperationException ex) {
            lblCapsLock.setVisible(false);
        }
    }

    private void showError(String message, boolean markUser, boolean markPass) {
        lblError.setText("<html>" + message + "</html>");
        lblError.setVisible(true);
        txtUsername.putClientProperty(FlatClientProperties.OUTLINE,
                markUser ? FlatClientProperties.OUTLINE_ERROR : null);
        txtPassword.putClientProperty(FlatClientProperties.OUTLINE,
                markPass ? FlatClientProperties.OUTLINE_ERROR : null);
    }

    private void clearErrorState() {
        if (lblError.isVisible()) {
            lblError.setVisible(false);
        }
        txtUsername.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtPassword.putClientProperty(FlatClientProperties.OUTLINE, null);
    }

    private void setLoadingState(boolean loading) {
        btnLogin.setEnabled(!loading);
        txtUsername.setEnabled(!loading);
        txtPassword.setEnabled(!loading);
        chkRemember.setEnabled(!loading);
        btnLogin.setText(loading ? "Đang xác thực..." : "Đăng nhập");
    }

    private void xuLyDangNhap() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        if (user.isEmpty() && pass.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập và mật khẩu.", true, true);
            txtUsername.requestFocusInWindow();
            return;
        }
        if (user.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập.", true, false);
            txtUsername.requestFocusInWindow();
            return;
        }
        if (pass.isEmpty()) {
            showError("Vui lòng nhập mật khẩu.", false, true);
            txtPassword.requestFocusInWindow();
            return;
        }

        clearErrorState();
        setLoadingState(true);

        SwingWorker<NhanVienDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected NhanVienDTO doInBackground() {
                return nhanVienBUS.dangNhap(user, pass);
            }

            @Override
            protected void done() {
                setLoadingState(false);
                try {
                    NhanVienDTO nv = get();
                    if (nv != null) {
                        saveRememberedUsername(chkRemember.isSelected() ? user : "");
                        Session.currentUser = nv;
                        dispose();
                        new MainFrame(nv.getHoTen(), nv.getVaiTro()).setVisible(true);
                    } else {
                        showError("Sai tài khoản, mật khẩu hoặc tài khoản đã bị khóa.", true, true);
                        txtPassword.selectAll();
                        txtPassword.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    showError("Không thể kết nối cơ sở dữ liệu. Vui lòng thử lại.", false, false);
                }
            }
        };
        worker.execute();
    }

    private Icon createLibraryBadgeIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(PRIMARY_BLUE);
                g2.fill(new RoundRectangle2D.Double(x, y, 52, 52, 16, 16));

                // Biểu tượng sách mở tối giản
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 12, y + 14, 13, 22, 3, 3);
                g2.drawRoundRect(x + 27, y + 14, 13, 22, 3, 3);
                g2.drawLine(x + 16, y + 20, x + 21, y + 20);
                g2.drawLine(x + 16, y + 25, x + 21, y + 25);
                g2.drawLine(x + 31, y + 20, x + 36, y + 20);
                g2.drawLine(x + 31, y + 25, x + 36, y + 25);
                g2.dispose();
            }

            @Override public int getIconWidth() { return 52; }
            @Override public int getIconHeight() { return 52; }
        };
    }

    private Icon createCheckBadgeIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(56, 189, 248, 45));
                g2.fillOval(x, y, 20, 20);
                g2.setColor(new Color(56, 189, 248));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 6, y + 10, x + 9, y + 13);
                g2.drawLine(x + 9, y + 13, x + 14, y + 7);
                g2.dispose();
            }

            @Override public int getIconWidth() { return 20; }
            @Override public int getIconHeight() { return 20; }
        };
    }

    private Icon createUserIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEXT_SECONDARY);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 5, y + 2, 6, 6);
                g2.drawArc(x + 2, y + 10, 12, 8, 0, 180);
                g2.dispose();
            }

            @Override public int getIconWidth() { return 16; }
            @Override public int getIconHeight() { return 16; }
        };
    }

    private Icon createLockIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEXT_SECONDARY);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 3, y + 7, 10, 7, 2, 2);
                g2.drawArc(x + 5, y + 2, 6, 8, 0, 180);
                g2.dispose();
            }

            @Override public int getIconWidth() { return 16; }
            @Override public int getIconHeight() { return 16; }
        };
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
