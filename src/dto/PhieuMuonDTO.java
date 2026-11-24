package dto;

import java.time.LocalDate;

public class PhieuMuonDTO {
    private int maPhieuMuon;
    private int maDocGia;
    private String tenDocGia;
    private int maNV;
    private String tenNV;
    private LocalDate ngayMuon;
    private LocalDate hanTra;
    private LocalDate ngayTraThucTe;
    private String trangThai;
    private double tongTienPhat;

    public PhieuMuonDTO() {
    }

    public PhieuMuonDTO(int maPhieuMuon, int maDocGia, String tenDocGia, int maNV, String tenNV,
                        LocalDate ngayMuon, LocalDate hanTra, LocalDate ngayTraThucTe,
                        String trangThai, double tongTienPhat) {
        this.maPhieuMuon = maPhieuMuon;
        this.maDocGia = maDocGia;
        this.tenDocGia = tenDocGia;
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.ngayMuon = ngayMuon;
        this.hanTra = hanTra;
        this.ngayTraThucTe = ngayTraThucTe;
        this.trangThai = trangThai;
        this.tongTienPhat = tongTienPhat;
    }

    public int getMaPhieuMuon() { return maPhieuMuon; }
    public void setMaPhieuMuon(int maPhieuMuon) { this.maPhieuMuon = maPhieuMuon; }

    public int getMaDocGia() { return maDocGia; }
    public void setMaDocGia(int maDocGia) { this.maDocGia = maDocGia; }

    public String getTenDocGia() { return tenDocGia; }
    public void setTenDocGia(String tenDocGia) { this.tenDocGia = tenDocGia; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getTenNV() { return tenNV; }
    public void setTenNV(String tenNV) { this.tenNV = tenNV; }

    public LocalDate getNgayMuon() { return ngayMuon; }
    public void setNgayMuon(LocalDate ngayMuon) { this.ngayMuon = ngayMuon; }

    public LocalDate getHanTra() { return hanTra; }
    public void setHanTra(LocalDate hanTra) { this.hanTra = hanTra; }

    public LocalDate getNgayTraThucTe() { return ngayTraThucTe; }
    public void setNgayTraThucTe(LocalDate ngayTraThucTe) { this.ngayTraThucTe = ngayTraThucTe; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public double getTongTienPhat() { return tongTienPhat; }
    public void setTongTienPhat(double tongTienPhat) { this.tongTienPhat = tongTienPhat; }
}