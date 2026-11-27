package utils;

import dto.ChiTietPhieuMuonDTO;
import dto.PhieuMuonDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tiện ích xem trước / in phiếu mượn - biên lai trả sách và xuất báo cáo CSV (UTF-8 BOM cho Excel).
 */
public class PrintExportUtil {

    public static void exportTableToCSV(Component parent, TableModel model, String defaultFileName) {
        if (model == null || model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parent, "Chưa có dữ liệu trong bảng để xuất!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn nơi lưu file báo cáo CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV (UTF-8 Excel) (*.csv)", "csv"));
        String safeName = (defaultFileName != null && !defaultFileName.trim().isEmpty())
                ? defaultFileName.trim()
                : "BaoCao";
        if (!safeName.toLowerCase().endsWith(".csv")) {
            safeName += ".csv";
        }
        chooser.setSelectedFile(new File(safeName));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }

        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    parent,
                    "File \"" + file.getName() + "\" đã tồn tại. Bạn có muốn ghi đè không?",
                    "Xác nhận ghi đè",
                    JOptionPane.YES_NO_OPTION
            );
            if (overwrite != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {
            // Ghi UTF-8 BOM để Microsoft Excel nhận diện đúng tiếng Việt có dấu
            writer.write('\uFEFF');
            writeTableModelAsCsv(model, writer);
            writer.flush();

            JOptionPane.showMessageDialog(parent,
                    "Đã xuất báo cáo thành công tới:\n" + file.getAbsolutePath(),
                    "Xuất CSV thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent,
                    "Không thể ghi file CSV: " + ex.getMessage(),
                    "Lỗi xuất file", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void writeTableModelAsCsv(TableModel model, Writer writer) throws IOException {
        int colCount = model.getColumnCount();
        int rowCount = model.getRowCount();

        for (int c = 0; c < colCount; c++) {
            if (c > 0) writer.write(',');
            writer.write(escapeCsvField(model.getColumnName(c)));
        }
        writer.write("\r\n");

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                if (c > 0) writer.write(',');
                Object val = model.getValueAt(r, c);
                writer.write(escapeCsvField(val != null ? val.toString() : ""));
            }
            writer.write("\r\n");
        }
    }

    public static String escapeCsvField(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        if (!needsQuotes) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    public static void showReceiptPreviewAndPrint(Component parent, PhieuMuonDTO pm,
                                                  List<ChiTietPhieuMuonDTO> details, boolean isReturnReceipt) {
        if (pm == null) {
            JOptionPane.showMessageDialog(parent, "Không tìm thấy thông tin phiếu để in!",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String html = buildReceiptHtml(pm, details, isReturnReceipt);
        JEditorPane editorPane = new JEditorPane("text/html", html);
        editorPane.setEditable(false);
        editorPane.setCaretPosition(0);

        Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        String dialogTitle = isReturnReceipt
                ? "Xem trước Biên lai trả sách #" + pm.getMaPhieuMuon()
                : "Xem trước Phiếu mượn #" + pm.getMaPhieuMuon();

        JDialog dialog = new JDialog(owner, dialogTitle, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(660, 680);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(10, 10));

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(new EmptyBorder(10, 10, 0, 10));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnPrint = new JButton("In / Lưu PDF");
        btnPrint.setBackground(new Color(37, 99, 235));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPrint.setFocusPainted(false);

        JButton btnClose = new JButton("Đóng");
        btnClose.setBackground(new Color(100, 116, 139));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setFocusPainted(false);

        btnPrint.addActionListener(e -> {
            try {
                boolean done = editorPane.print(null, null, true, null, null, true);
                if (done) {
                    JOptionPane.showMessageDialog(dialog, "Đã gửi lệnh in thành công!",
                            "In phiếu", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi khi in: " + ex.getMessage(),
                        "Lỗi in", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnClose.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnPrint);
        btnPanel.add(btnClose);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public static String buildReceiptHtml(PhieuMuonDTO pm, List<ChiTietPhieuMuonDTO> details, boolean isReturnReceipt) {
        String title = isReturnReceipt ? "BIÊN LAI TRẢ SÁCH & TIỀN PHẠT" : "PHIẾU MƯỢN SÁCH THƯ VIỆN";
        String printTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: Segoe UI, Arial, sans-serif; font-size: 12px; color: #1e293b; padding: 16px;'>");
        sb.append("<div style='text-align: center; border-bottom: 2px solid #334155; padding-bottom: 8px; margin-bottom: 12px;'>");
        sb.append("<div style='font-size: 11px; color: #64748b;'>HỆ THỐNG QUẢN LÝ THƯ VIỆN</div>");
        sb.append("<div style='font-size: 17px; font-weight: bold; margin-top: 4px;'>").append(title).append("</div>");
        sb.append("<div style='font-size: 12px; margin-top: 2px;'>Mã phiếu: <b>#").append(pm.getMaPhieuMuon()).append("</b></div>");
        sb.append("</div>");

        sb.append("<table style='width: 100%; border-collapse: collapse; margin-bottom: 12px;'>");
        sb.append("<tr><td style='padding: 3px 0;'><b>Độc giả:</b> ").append(escapeHtml(pm.getTenDocGia())).append(" (Mã ĐG: ").append(pm.getMaDocGia()).append(")</td>");
        sb.append("<td style='padding: 3px 0;'><b>Nhân viên:</b> ").append(escapeHtml(pm.getTenNV())).append("</td></tr>");
        sb.append("<tr><td style='padding: 3px 0;'><b>Ngày mượn:</b> ").append(pm.getNgayMuon() != null ? pm.getNgayMuon() : "-").append("</td>");
        sb.append("<td style='padding: 3px 0;'><b>Hạn trả:</b> ").append(pm.getHanTra() != null ? pm.getHanTra() : "-").append("</td></tr>");
        sb.append("<tr><td style='padding: 3px 0;'><b>Trạng thái:</b> ").append(escapeHtml(pm.getTrangThai())).append("</td>");
        sb.append("<td style='padding: 3px 0;'><b>Số lần gia hạn:</b> ").append(pm.getSoLanGiaHan()).append("</td></tr>");
        if (isReturnReceipt || pm.getNgayTraThucTe() != null) {
            sb.append("<tr><td style='padding: 3px 0;'><b>Ngày trả thực tế:</b> ")
              .append(pm.getNgayTraThucTe() != null ? pm.getNgayTraThucTe() : "-")
              .append("</td><td style='padding: 3px 0;'><b>Ngày in:</b> ").append(printTime).append("</td></tr>");
        } else {
            sb.append("<tr><td colspan='2' style='padding: 3px 0;'><b>Thời điểm in:</b> ").append(printTime).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<table style='width: 100%; border-collapse: collapse; margin-bottom: 12px;' border='1' cellspacing='0' cellpadding='6'>");
        sb.append("<tr style='background-color: #f1f5f9;'>");
        sb.append("<th align='center'>STT</th>");
        sb.append("<th align='center'>Mã sách</th>");
        sb.append("<th align='left'>Tên sách</th>");
        sb.append("<th align='center'>SL</th>");
        sb.append("<th align='left'>").append(isReturnReceipt ? "Tình trạng trả" : "Tình trạng mượn").append("</th>");
        if (isReturnReceipt) {
            sb.append("<th align='right'>Phạt sách (VNĐ)</th>");
        }
        sb.append("</tr>");

        int totalQty = 0;
        if (details != null) {
            for (int i = 0; i < details.size(); i++) {
                ChiTietPhieuMuonDTO ct = details.get(i);
                totalQty += ct.getSoLuong();
                String tinhTrang = isReturnReceipt
                        ? (ct.getTinhTrangKhiTra() != null ? ct.getTinhTrangKhiTra() : "Bình thường")
                        : (ct.getTinhTrangKhiMuon() != null ? ct.getTinhTrangKhiMuon() : "Bình thường");
                sb.append("<tr>");
                sb.append("<td align='center'>").append(i + 1).append("</td>");
                sb.append("<td align='center'>").append(ct.getMaSach()).append("</td>");
                sb.append("<td>").append(escapeHtml(ct.getTenSach())).append("</td>");
                sb.append("<td align='center'>").append(ct.getSoLuong()).append("</td>");
                sb.append("<td>").append(escapeHtml(tinhTrang)).append("</td>");
                if (isReturnReceipt) {
                    sb.append("<td align='right'>").append(String.format("%,.0f", ct.getTienPhat())).append("</td>");
                }
                sb.append("</tr>");
            }
        }
        sb.append("</table>");

        sb.append("<div style='text-align: right; margin-bottom: 18px;'>");
        sb.append("<div><b>Tổng số cuốn:</b> ").append(totalQty).append(" cuốn</div>");
        if (isReturnReceipt || pm.getTongTienPhat() > 0) {
            sb.append("<div style='font-size: 14px; color: #b91c1c; margin-top: 4px;'><b>TỔNG TIỀN PHẠT: ")
              .append(String.format("%,.0f VNĐ", pm.getTongTienPhat())).append("</b></div>");
        }
        sb.append("</div>");

        sb.append("<table style='width: 100%; text-align: center; margin-top: 20px;'>");
        sb.append("<tr><td><b>Độc giả</b><br/><i>(Ký và ghi rõ họ tên)</i></td>");
        sb.append("<td><b>Thủ thư thực hiện</b><br/><i>(Ký và ghi rõ họ tên)</i></td></tr>");
        sb.append("</table>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
