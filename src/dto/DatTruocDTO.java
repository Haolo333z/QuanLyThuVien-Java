package dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DatTruocDTO {
    private int maDatTruoc;
    private int maDocGia;
    private String tenDocGia;
    private String sdtDocGia;
    private int maSach;
    private String tenSach;
    private int maNV;
    private String tenNV;
    private LocalDateTime ngayDat;
    private LocalDate hanGiuCho;
    private String trangThai;
    private String ghiChu;

    public DatTruocDTO() {
    }

    public DatTruocDTO(int maDatTruoc, int maDocGia, String tenDocGia, String sdtDocGia,
                       int maSach, String tenSach, int maNV, String tenNV,
                       LocalDateTime ngayDat, LocalDate hanGiuCho, String trangThai, String ghiChu) {
        this.maDatTruoc = maDatTruoc;
        this.maDocGia = maDocGia;
        this.tenDocGia = tenDocGia;
        this.sdtDocGia = sdtDocGia;
        this.maSach = maSach;
        this.tenSach = tenSach;
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.ngayDat = ngayDat;
        this.hanGiuCho = hanGiuCho;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public int getMaDatTruoc() { return maDatTruoc; }
    public void setMaDatTruoc(int maDatTruoc) { this.maDatTruoc = maDatTruoc; }

    public int getMaDocGia() { return maDocGia; }
    public void setMaDocGia(int maDocGia) { this.maDocGia = maDocGia; }

    public String getTenDocGia() { return tenDocGia; }
    public void setTenDocGia(String tenDocGia) { this.tenDocGia = tenDocGia; }

    public String getSdtDocGia() { return sdtDocGia; }
    public void setSdtDocGia(String sdtDocGia) { this.sdtDocGia = sdtDocGia; }

    public int getMaSach() { return maSach; }
    public void setMaSach(int maSach) { this.maSach = maSach; }

    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getTenNV() { return tenNV; }
    public void setTenNV(String tenNV) { this.tenNV = tenNV; }

    public LocalDateTime getNgayDat() { return ngayDat; }
    public void setNgayDat(LocalDateTime ngayDat) { this.ngayDat = ngayDat; }

    public LocalDate getHanGiuCho() { return hanGiuCho; }
    public void setHanGiuCho(LocalDate hanGiuCho) { this.hanGiuCho = hanGiuCho; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
