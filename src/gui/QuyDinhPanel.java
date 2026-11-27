package gui;

import bus.QuyDinhBUS;
import dto.QuyDinhDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class QuyDinhPanel extends JPanel {
    private QuyDinhBUS quyDinhBUS = new QuyDinhBUS();

    private JTextField txtSoNgayMuon;
    private JTextField txtSoNgayGiaHan;
    private JTextField txtSoLanGiaHanToiDa;
    private JTextField txtSoSachToiDa;
    private JTextField txtPhatTreMoiNgay;
    private JTextField txtPhatHuHong;
    private JTextField txtPhatMatSach;
    private int currentMaQuyDinh = 1;

    public QuyDinhPanel() {
        initUI();
        refreshData();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 250, 252));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("QUY ĐỊNH & MỨC PHẠT THƯ VIỆN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel cardPanel = new JPanel(new BorderLayout(15, 15));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("Cấu hình tham số vận hành (Áp dụng cho toàn hệ thống)"),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JPanel gridContainer = new JPanel(new GridLayout(1, 2, 24, 0));
        gridContainer.setBackground(Color.WHITE);

        // Nhóm 1: Mượn & Gia hạn
        JPanel loanRulesPanel = new JPanel(new GridLayout(4, 2, 12, 14));
        loanRulesPanel.setBackground(Color.WHITE);
        loanRulesPanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("1. Quy định Mượn & Gia hạn"),
                new EmptyBorder(12, 12, 12, 12)
        ));

        txtSoNgayMuon = createInputField();
        txtSoNgayGiaHan = createInputField();
        txtSoLanGiaHanToiDa = createInputField();
        txtSoSachToiDa = createInputField();

        loanRulesPanel.add(createFieldLabel("Số ngày mượn mặc định:"));
        loanRulesPanel.add(txtSoNgayMuon);
        loanRulesPanel.add(createFieldLabel("Số ngày gia hạn / lần:"));
        loanRulesPanel.add(txtSoNgayGiaHan);
        loanRulesPanel.add(createFieldLabel("Số lần gia hạn tối đa:"));
        loanRulesPanel.add(txtSoLanGiaHanToiDa);
        loanRulesPanel.add(createFieldLabel("Số cuốn mượn tối đa / ĐG:"));
        loanRulesPanel.add(txtSoSachToiDa);

        // Nhóm 2: Mức phạt
        JPanel fineRulesPanel = new JPanel(new GridLayout(4, 2, 12, 14));
        fineRulesPanel.setBackground(Color.WHITE);
        fineRulesPanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("2. Quy định Mức phạt (VNĐ)"),
                new EmptyBorder(12, 12, 12, 12)
        ));

        txtPhatTreMoiNgay = createInputField();
        txtPhatHuHong = createInputField();
        txtPhatMatSach = createInputField();

        fineRulesPanel.add(createFieldLabel("Phạt trễ hạn mỗi ngày (VNĐ):"));
        fineRulesPanel.add(txtPhatTreMoiNgay);
        fineRulesPanel.add(createFieldLabel("Phạt sách hư hỏng (VNĐ/cuốn):"));
        fineRulesPanel.add(txtPhatHuHong);
        fineRulesPanel.add(createFieldLabel("Phạt mất sách (VNĐ/cuốn):"));
        fineRulesPanel.add(txtPhatMatSach);
        fineRulesPanel.add(new JLabel(""));
        fineRulesPanel.add(new JLabel(""));

        gridContainer.add(loanRulesPanel);
        gridContainer.add(fineRulesPanel);

        JLabel lblNote = new JLabel(
                "Lưu ý: Quy định mới áp dụng cho các lượt mượn, gia hạn và trả sách thực hiện sau khi lưu; không hồi tố các phiếu đã hoàn tất."
        );
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblNote.setForeground(new Color(100, 116, 139));
        lblNote.setBorder(new EmptyBorder(8, 0, 4, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnMacDinh = createActionButton("Điền giá trị mặc định", new Color(100, 116, 139));
        JButton btnTaiLai = createActionButton("Tải lại từ CSDL", new Color(14, 116, 144));
        JButton btnLuu = createActionButton("Lưu quy định", new Color(22, 163, 74));

        buttonPanel.add(btnMacDinh);
        buttonPanel.add(btnTaiLai);
        buttonPanel.add(btnLuu);

        JPanel bottomCard = new JPanel(new BorderLayout(0, 8));
        bottomCard.setBackground(Color.WHITE);
        bottomCard.add(lblNote, BorderLayout.NORTH);
        bottomCard.add(buttonPanel, BorderLayout.SOUTH);

        cardPanel.add(gridContainer, BorderLayout.CENTER);
        cardPanel.add(bottomCard, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(cardPanel, BorderLayout.NORTH);

        add(topPanel, BorderLayout.NORTH);
        add(wrapper, BorderLayout.CENTER);

        btnTaiLai.addActionListener(e -> refreshData());
        btnMacDinh.addActionListener(e -> fillForm(new QuyDinhDTO()));
        btnLuu.addActionListener(e -> xuLyLuuQuyDinh());
    }

    public void refreshData() {
        fillForm(quyDinhBUS.getQuyDinh());
    }

    private void fillForm(QuyDinhDTO qd) {
        currentMaQuyDinh = qd.getMaQuyDinh() > 0 ? qd.getMaQuyDinh() : 1;
        txtSoNgayMuon.setText(String.valueOf(qd.getSoNgayMuonMacDinh()));
        txtSoNgayGiaHan.setText(String.valueOf(qd.getSoNgayGiaHanMacDinh()));
        txtSoLanGiaHanToiDa.setText(String.valueOf(qd.getSoLanGiaHanToiDa()));
        txtSoSachToiDa.setText(String.valueOf(qd.getSoSachToiDa()));
        txtPhatTreMoiNgay.setText(String.valueOf(qd.getPhatTreMoiNgay()));
        txtPhatHuHong.setText(String.valueOf(qd.getPhatHuHong()));
        txtPhatMatSach.setText(String.valueOf(qd.getPhatMatSach()));
    }

    private void xuLyLuuQuyDinh() {
        try {
            QuyDinhDTO qd = new QuyDinhDTO();
            qd.setMaQuyDinh(currentMaQuyDinh);
            qd.setSoNgayMuonMacDinh(Integer.parseInt(txtSoNgayMuon.getText().trim()));
            qd.setSoNgayGiaHanMacDinh(Integer.parseInt(txtSoNgayGiaHan.getText().trim()));
            qd.setSoLanGiaHanToiDa(Integer.parseInt(txtSoLanGiaHanToiDa.getText().trim()));
            qd.setSoSachToiDa(Integer.parseInt(txtSoSachToiDa.getText().trim()));
            qd.setPhatTreMoiNgay(Long.parseLong(txtPhatTreMoiNgay.getText().trim()));
            qd.setPhatHuHong(Long.parseLong(txtPhatHuHong.getText().trim()));
            qd.setPhatMatSach(Long.parseLong(txtPhatMatSach.getText().trim()));

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận lưu thay đổi quy định thư viện?",
                    "Xác nhận lưu",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            String msg = quyDinhBUS.updateQuyDinh(qd);
            if ("OK".equals(msg)) {
                JOptionPane.showMessageDialog(this, "Đã lưu quy định thư viện thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, msg, "Không thể lưu", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Tất cả các trường quy định phải là số nguyên hợp lệ!",
                    "Sai định dạng số", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JTextField createInputField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return txt;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lbl;
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
}
