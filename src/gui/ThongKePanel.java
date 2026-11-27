package gui;

import bus.ThongKeBUS;
import utils.PrintExportUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ThongKePanel extends JPanel {
    private ThongKeBUS thongKeBUS = new ThongKeBUS();

    private JLabel lblTongSach, lblTongDocGia, lblDangMuon, lblTongPhat, lblPhatDuKien;
    private DefaultTableModel topSachModel, quaHanModel;

    // Tab 2: Thống kê theo kỳ & Thể loại
    private JTextField txtTuNgay, txtDenNgay;
    private JLabel lblKyMuon, lblKyTra, lblKyPhat;
    private DefaultTableModel theoThangModel, theoTheLoaiModel;
    private MonthlyBarChartPanel chartPanel;

    public ThongKePanel() {
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 250, 252));

        // 1. HEADER & NÚT LÀM MỚI
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("THỐNG KÊ & TỔNG QUAN THƯ VIỆN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));

        JButton btnRefresh = new JButton("Làm mới số liệu");
        btnRefresh.setBackground(new Color(37, 99, 235));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadData());

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(btnRefresh, BorderLayout.EAST);

        // 2. 5 THẺ KPI TỔNG QUAN
        JPanel cardsPanel = new JPanel(new GridLayout(1, 5, 12, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 105));

        lblTongSach = new JLabel("0");
        lblTongDocGia = new JLabel("0");
        lblDangMuon = new JLabel("0");
        lblTongPhat = new JLabel("0 VNĐ");
        lblPhatDuKien = new JLabel("0 VNĐ");

        cardsPanel.add(createStatCard("TỔNG ĐẦU SÁCH", lblTongSach, new Color(37, 99, 235)));
        cardsPanel.add(createStatCard("ĐỘC GIẢ HOẠT ĐỘNG", lblTongDocGia, new Color(22, 163, 74)));
        cardsPanel.add(createStatCard("PHIẾU ĐANG MƯỢN", lblDangMuon, new Color(234, 88, 12)));
        cardsPanel.add(createStatCard("TỔNG PHẠT ĐÃ THU", lblTongPhat, new Color(220, 38, 38)));
        cardsPanel.add(createStatCard("PHẠT DỰ KIẾN QUÁ HẠN", lblPhatDuKien, new Color(147, 51, 234)));

        // 3. TAB NỘI DUNG
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("Tổng quan & Quá hạn", createOverviewTab());
        tabbedPane.addTab("Thống kê theo kỳ & Thể loại", createPeriodAnalyticsTab());

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(tabbedPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createOverviewTab() {
        topSachModel = new DefaultTableModel(new String[]{"Mã sách", "Tên sách", "Tác giả", "Tổng lượt mượn"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblTopSach = new JTable(topSachModel);
        tblTopSach.setRowHeight(28);
        JPanel topSachCard = createTableSection("Top sách được mượn nhiều nhất", tblTopSach, "TopSachMuonNhieu");

        quaHanModel = new DefaultTableModel(new String[]{"Mã phiếu", "Độc giả", "SĐT", "Ngày mượn", "Hạn trả", "Quá hạn"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblQuaHan = new JTable(quaHanModel);
        tblQuaHan.setRowHeight(28);
        JPanel quaHanCard = createTableSection("Danh sách phiếu mượn quá hạn chưa trả", tblQuaHan, "PhieuMuonQuaHan");

        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        tablesPanel.setOpaque(false);
        tablesPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        tablesPanel.add(topSachCard);
        tablesPanel.add(quaHanCard);
        return tablesPanel;
    }

    private JPanel createPeriodAnalyticsTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Thanh lọc Từ ngày - Đến ngày + KPI trong kỳ
        JPanel topFilterBar = new JPanel(new BorderLayout(10, 8));
        topFilterBar.setBackground(Color.WHITE);
        topFilterBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JPanel filterInputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterInputs.setOpaque(false);
        LocalDate defaultTo = LocalDate.now();
        LocalDate defaultFrom = defaultTo.minusMonths(5).withDayOfMonth(1);

        txtTuNgay = new JTextField(defaultFrom.toString(), 10);
        txtDenNgay = new JTextField(defaultTo.toString(), 10);
        JButton btnLocKy = new JButton("Lọc theo kỳ");
        btnLocKy.setBackground(new Color(37, 99, 235));
        btnLocKy.setForeground(Color.WHITE);
        btnLocKy.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLocKy.setFocusPainted(false);

        filterInputs.add(new JLabel("Từ ngày (yyyy-MM-dd):"));
        filterInputs.add(txtTuNgay);
        filterInputs.add(new JLabel("Đến ngày:"));
        filterInputs.add(txtDenNgay);
        filterInputs.add(btnLocKy);

        JPanel periodSummary = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        periodSummary.setOpaque(false);
        lblKyMuon = new JLabel("Phiếu mượn kỳ: 0");
        lblKyTra = new JLabel("Đã trả kỳ: 0");
        lblKyPhat = new JLabel("Phạt thu trong kỳ: 0 VNĐ");
        Font summaryFont = new Font("Segoe UI", Font.BOLD, 13);
        lblKyMuon.setFont(summaryFont);
        lblKyMuon.setForeground(new Color(37, 99, 235));
        lblKyTra.setFont(summaryFont);
        lblKyTra.setForeground(new Color(22, 163, 74));
        lblKyPhat.setFont(summaryFont);
        lblKyPhat.setForeground(new Color(220, 38, 38));
        periodSummary.add(lblKyMuon);
        periodSummary.add(lblKyTra);
        periodSummary.add(lblKyPhat);

        topFilterBar.add(filterInputs, BorderLayout.WEST);
        topFilterBar.add(periodSummary, BorderLayout.EAST);

        // Bên trái: Biểu đồ cột + Bảng theo tháng
        chartPanel = new MonthlyBarChartPanel();
        chartPanel.setPreferredSize(new Dimension(0, 190));
        chartPanel.setBorder(new TitledBorder("Biểu đồ lượt phiếu mượn theo tháng"));

        theoThangModel = new DefaultTableModel(new String[]{"Tháng", "Phiếu mượn", "Phiếu đã trả", "Tiền phạt (VNĐ)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblThang = new JTable(theoThangModel);
        tblThang.setRowHeight(26);
        JPanel thangTableCard = createTableSection("Chi tiết theo tháng", tblThang, "ThongKeTheoThang");

        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setOpaque(false);
        leftPanel.add(chartPanel, BorderLayout.NORTH);
        leftPanel.add(thangTableCard, BorderLayout.CENTER);

        // Bên phải: Bảng thống kê theo Thể loại
        theoTheLoaiModel = new DefaultTableModel(
                new String[]{"Thể loại", "Đầu sách", "Tổng bản", "Còn lại", "Thanh lý", "Lượt mượn"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblTheLoai = new JTable(theoTheLoaiModel);
        tblTheLoai.setRowHeight(28);
        JPanel theLoaiCard = createTableSection("Thống kê sách & lượt mượn theo Thể loại", tblTheLoai, "ThongKeTheoTheLoai");

        JPanel bodyGrid = new JPanel(new GridLayout(1, 2, 15, 0));
        bodyGrid.setOpaque(false);
        bodyGrid.add(leftPanel);
        bodyGrid.add(theLoaiCard);

        panel.add(topFilterBar, BorderLayout.NORTH);
        panel.add(bodyGrid, BorderLayout.CENTER);

        btnLocKy.addActionListener(e -> loadPeriodData(true));
        return panel;
    }

    private JPanel createTableSection(String title, JTable table, String csvPrefix) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 12, 12, 12)
        ));

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(30, 41, 59));

        JButton btnExport = new JButton("Xuất CSV (Excel)");
        btnExport.setBackground(new Color(22, 163, 74));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExport.setFocusPainted(false);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.addActionListener(e ->
                PrintExportUtil.exportTableToCSV(this, table.getModel(), csvPrefix + "_" + LocalDate.now())
        );

        header.add(lbl, BorderLayout.WEST);
        header.add(btnExport, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
                new EmptyBorder(15, 16, 15, 14)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 21));
        valueLabel.setForeground(new Color(15, 23, 42));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        loadData();
    }

    public void loadData() {
        lblTongSach.setText(String.valueOf(thongKeBUS.getTongSoDauSach()));
        lblTongDocGia.setText(String.valueOf(thongKeBUS.getTongSoDocGia()));
        lblDangMuon.setText(String.valueOf(thongKeBUS.getSoPhieuDangMuon()));
        lblTongPhat.setText(String.format("%,.0f VNĐ", thongKeBUS.getTongTienPhat()));
        lblPhatDuKien.setText(String.format("%,.0f VNĐ", thongKeBUS.getTienPhatDuKienQuaHan()));

        topSachModel.setRowCount(0);
        for (Object[] row : thongKeBUS.getTopSachMuonNhieu()) {
            topSachModel.addRow(row);
        }

        quaHanModel.setRowCount(0);
        for (Object[] row : thongKeBUS.getDanhSachQuaHan()) {
            quaHanModel.addRow(row);
        }

        loadPeriodData(false);
    }

    private void loadPeriodData(boolean showErrors) {
        if (txtTuNgay == null || txtDenNgay == null) {
            return;
        }
        try {
            LocalDate tu = LocalDate.parse(txtTuNgay.getText().trim());
            LocalDate den = LocalDate.parse(txtDenNgay.getText().trim());
            if (tu.isAfter(den)) {
                if (showErrors) {
                    JOptionPane.showMessageDialog(this, "Từ ngày không được lớn hơn Đến ngày!",
                            "Khoảng thời gian không hợp lệ", JOptionPane.WARNING_MESSAGE);
                }
                return;
            }

            lblKyMuon.setText("Phiếu mượn kỳ: " + thongKeBUS.getSoPhieuMuonTheoKy(tu, den));
            lblKyTra.setText("Đã trả kỳ: " + thongKeBUS.getSoPhieuDaTraTheoKy(tu, den));
            lblKyPhat.setText(String.format("Phạt thu trong kỳ: %,.0f VNĐ", thongKeBUS.getTongTienPhatTheoKy(tu, den)));

            List<Object[]> monthlyRows = thongKeBUS.getThongKeTheoThang(tu, den);
            theoThangModel.setRowCount(0);
            List<String> chartLabels = new ArrayList<>();
            List<Integer> chartValues = new ArrayList<>();
            for (Object[] r : monthlyRows) {
                theoThangModel.addRow(r);
                chartLabels.add(String.valueOf(r[0]));
                chartValues.add((Integer) r[1]);
            }
            chartPanel.setData(chartLabels, chartValues);

            theoTheLoaiModel.setRowCount(0);
            for (Object[] r : thongKeBUS.getThongKeTheoTheLoai()) {
                theoTheLoaiModel.addRow(r);
            }
        } catch (DateTimeParseException ex) {
            if (showErrors) {
                JOptionPane.showMessageDialog(this, "Ngày lọc phải đúng định dạng yyyy-MM-dd!",
                        "Sai định dạng ngày", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private static class MonthlyBarChartPanel extends JPanel {
        private List<String> labels = new ArrayList<>();
        private List<Integer> values = new ArrayList<>();

        public MonthlyBarChartPanel() {
            setBackground(Color.WHITE);
        }

        public void setData(List<String> labels, List<Integer> values) {
            this.labels = labels != null ? labels : new ArrayList<>();
            this.values = values != null ? values : new ArrayList<>();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padLeft = 42;
            int padRight = 20;
            int padTop = 28;
            int padBottom = 34;

            if (labels.isEmpty()) {
                g2.setColor(new Color(100, 116, 139));
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                String msg = "Chưa có dữ liệu phiếu mượn trong khoảng thời gian đã chọn";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, Math.max(12, (w - fm.stringWidth(msg)) / 2), h / 2);
                g2.dispose();
                return;
            }

            int chartW = Math.max(1, w - padLeft - padRight);
            int chartH = Math.max(1, h - padTop - padBottom);
            int maxVal = 1;
            for (int v : values) {
                if (v > maxVal) maxVal = v;
            }

            // Trục & lưới ngang nhẹ
            g2.setColor(new Color(226, 232, 240));
            g2.drawLine(padLeft, padTop, padLeft, padTop + chartH);
            g2.drawLine(padLeft, padTop + chartH, padLeft + chartW, padTop + chartH);

            int count = labels.size();
            int slotW = Math.max(24, chartW / count);
            int barW = Math.min(44, Math.max(14, (int) (slotW * 0.55)));

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i < count; i++) {
                int val = values.get(i);
                int barH = (int) Math.round((double) val / maxVal * (chartH - 10));
                int x = padLeft + i * slotW + (slotW - barW) / 2;
                int y = padTop + chartH - barH;

                g2.setColor(new Color(37, 99, 235));
                g2.fillRoundRect(x, y, barW, barH, 6, 6);

                String valStr = String.valueOf(val);
                g2.setColor(new Color(30, 41, 59));
                g2.drawString(valStr, x + (barW - fm.stringWidth(valStr)) / 2, Math.max(padTop - 2, y - 4));

                String lbl = labels.get(i);
                g2.setColor(new Color(71, 85, 105));
                g2.drawString(lbl, x + (barW - fm.stringWidth(lbl)) / 2, padTop + chartH + 16);
            }

            g2.dispose();
        }
    }
}
