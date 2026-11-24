package gui;

import bus.ThongKeBUS;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ThongKePanel extends JPanel {
    private ThongKeBUS thongKeBUS = new ThongKeBUS();

    private JLabel lblTongSach, lblTongDocGia, lblDangMuon, lblTongPhat;
    private DefaultTableModel topSachModel, quaHanModel;

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

        // 2. 4 THẺ KPI TỔNG QUAN
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 110));

        lblTongSach = new JLabel("0");
        lblTongDocGia = new JLabel("0");
        lblDangMuon = new JLabel("0");
        lblTongPhat = new JLabel("0 VNĐ");

        cardsPanel.add(createStatCard("TỔNG ĐẦU SÁCH", lblTongSach, new Color(37, 99, 235)));
        cardsPanel.add(createStatCard("ĐỘC GIẢ HOẠT ĐỘNG", lblTongDocGia, new Color(22, 163, 74)));
        cardsPanel.add(createStatCard("PHIẾU ĐANG MƯỢN", lblDangMuon, new Color(234, 88, 12)));
        cardsPanel.add(createStatCard("TỔNG TIỀN PHẠT", lblTongPhat, new Color(220, 38, 38)));

        // 3. HAI BẢNG THỐNG KÊ CHI TIẾT
        topSachModel = new DefaultTableModel(new String[]{"Mã sách", "Tên sách", "Tác giả", "Tổng lượt mượn"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblTopSach = new JTable(topSachModel);
        tblTopSach.setRowHeight(28);
        JScrollPane spTopSach = new JScrollPane(tblTopSach);
        spTopSach.setBorder(new TitledBorder("Top sách được mượn nhiều nhất"));

        quaHanModel = new DefaultTableModel(new String[]{"Mã phiếu", "Độc giả", "SĐT", "Ngày mượn", "Hạn trả", "Quá hạn"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblQuaHan = new JTable(quaHanModel);
        tblQuaHan.setRowHeight(28);
        JScrollPane spQuaHan = new JScrollPane(tblQuaHan);
        spQuaHan.setBorder(new TitledBorder("Danh sách phiếu mượn quá hạn chưa trả"));

        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        tablesPanel.setOpaque(false);
        tablesPanel.add(spTopSach);
        tablesPanel.add(spQuaHan);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(tablesPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
                new EmptyBorder(15, 18, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(new Color(15, 23, 42));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void loadData() {
        lblTongSach.setText(String.valueOf(thongKeBUS.getTongSoDauSach()));
        lblTongDocGia.setText(String.valueOf(thongKeBUS.getTongSoDocGia()));
        lblDangMuon.setText(String.valueOf(thongKeBUS.getSoPhieuDangMuon()));
        lblTongPhat.setText(String.format("%,.0f VNĐ", thongKeBUS.getTongTienPhat()));

        topSachModel.setRowCount(0);
        for (Object[] row : thongKeBUS.getTopSachMuonNhieu()) {
            topSachModel.addRow(row);
        }

        quaHanModel.setRowCount(0);
        for (Object[] row : thongKeBUS.getDanhSachQuaHan()) {
            quaHanModel.addRow(row);
        }
    }
}